package com.next.nest.service.impl;

import com.next.nest.dto.*;
import com.next.nest.entity.Booking;
import com.next.nest.entity.Property;
import com.next.nest.entity.Transaction;
import com.next.nest.entity.User;
import com.next.nest.entity.enums.ListingStatus;
import com.next.nest.entity.enums.PaymentStatus;
import com.next.nest.entity.enums.TransactionType;
import com.next.nest.entity.enums.UserRole;
import com.next.nest.exception.BadRequestException;
import com.next.nest.exception.ResourceNotFoundException;
import com.next.nest.exception.UnauthorizedException;
import com.next.nest.repository.*;
import com.next.nest.service.PaymentService;
import com.next.nest.util.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final ServiceRepository serviceRepository;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public TransactionDTO findById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Security check: only the user involved in the transaction or admin can access it
        if (!isAuthorizedToAccessTransaction(transaction)) {
            throw new UnauthorizedException("You are not authorized to access this transaction");
        }

        return mapToDTO(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionDTO findByTransactionId(String transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        // Security check: only the user involved in the transaction or admin can access it
        if (!isAuthorizedToAccessTransaction(transaction)) {
            throw new UnauthorizedException("You are not authorized to access this transaction");
        }

        return mapToDTO(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findByUserId(Long userId, int page, int size) {
        // Security check: users can only see their own transactions
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(userId) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to access these transactions");
        }

        return transactionRepository.findByUserId(userId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findByBookingPropertyOwnerId(Long ownerId, int page, int size) {
        // Security check: only the property owner or admin can access these transactions
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(ownerId) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to access these transactions");
        }

        return transactionRepository.findByBookingPropertyOwnerId(ownerId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findByBookingId(Long bookingId, int page, int size) {
        // Verify booking exists
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));

        // Security check: only the tenant, property owner, or admin can access these transactions
        User currentUser = getCurrentUser();
        if (!booking.getTenant().getId().equals(currentUser.getId()) &&
                !booking.getProperty().getOwner().getId().equals(currentUser.getId()) &&
                !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to access these transactions");
        }

        return transactionRepository.findByBookingId(bookingId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findByUserIdAndStatus(Long userId, PaymentStatus status, int page, int size) {
        // Security check: users can only see their own transactions
        User currentUser = getCurrentUser();
        if (!currentUser.getId().equals(userId) && !isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to access these transactions");
        }

        return transactionRepository.findByUserIdAndStatus(userId, status.name(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDTO> findByType(TransactionType type, int page, int size) {
        // Security check: only admin can access all transactions by type
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to access these transactions");
        }

        return transactionRepository.findByType(type.name(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::mapToDTO);
    }

    @Override
    @Transactional
    public TransactionDTO initiatePayment(TransactionDTO transactionDTO) {
        // Validate input
        if (transactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than 0");
        }

        // Get the current user
        User user = getCurrentUser();

        // Validate booking if present
        Booking booking = null;
        if (transactionDTO.getBookingId() != null) {
            booking = bookingRepository.findById(transactionDTO.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + transactionDTO.getBookingId()));

            // Security check: only the tenant can make a payment for a booking
            if (!booking.getTenant().getId().equals(user.getId())) {
                throw new UnauthorizedException("You are not authorized to make a payment for this booking");
            }
        }

        // Validate service if present
        com.next.nest.entity.Service serviceEntity = null;
        if (transactionDTO.getServiceId() != null) {
            serviceEntity = serviceRepository.findById(transactionDTO.getServiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + transactionDTO.getServiceId()));
        }

        // Calculate fees and taxes
        BigDecimal fees = calculateFees(transactionDTO.getAmount(), transactionDTO.getType());
        BigDecimal tax = calculateTax(transactionDTO.getAmount(), transactionDTO.getType());
        BigDecimal totalAmount = transactionDTO.getAmount().add(fees).add(tax);

        // Generate unique transaction ID
        String transactionId = generateUniqueTransactionId();

        // Create transaction
        Transaction transaction = Transaction.builder()
                .transactionId(transactionId)
                .user(user)
                .booking(booking)
                .service(serviceEntity)
                .type(transactionDTO.getType())
                .amount(transactionDTO.getAmount())
                .paymentMethod(transactionDTO.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .description(transactionDTO.getDescription())
                .fees(fees)
                .tax(tax)
                .totalAmount(totalAmount)
                .isRefundable(transactionDTO.isRefundable())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        // In a real system, you would integrate with a payment gateway here
        // and redirect the user to the payment page or return payment link

        return mapToDTO(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionDTO completePayment(String transactionId, Map<String, Object> paymentDetails) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        // In a real system, you would verify the payment with the payment gateway
        // and update the transaction status accordingly

        // For demo purposes, we'll just update the status to SUCCESS
        transaction.setStatus(PaymentStatus.SUCCESS);
        transaction.setPaymentDate(LocalDateTime.now());
        transaction.setReferenceId((String) paymentDetails.get("referenceId"));
        transaction.setPaymentGatewayResponse(paymentDetails.toString());

        // Generate receipt
        String receiptUrl = generatePaymentReceipt(transactionId);
        transaction.setReceiptUrl(receiptUrl);

        Transaction updatedTransaction = transactionRepository.save(transaction);

        // Send payment confirmation email
        sendPaymentConfirmationEmail(updatedTransaction);

        // If this is a property-related payment, update the property status if needed
        if (transaction.getBooking() != null &&
                (transaction.getType() == TransactionType.SECURITY_DEPOSIT ||
                        transaction.getType() == TransactionType.RENT_PAYMENT)) {
            updatePropertyStatusAfterPayment(transaction.getBooking().getProperty(), transaction.getType());
        }

        return mapToDTO(updatedTransaction);
    }

    @Override
    @Transactional
    public TransactionDTO processPaymentCallback(Map<String, Object> callbackData) {
        // This method would handle callbacks from payment gateways
        // For demo purposes, we'll extract the transaction ID and update the status

        String transactionId = (String) callbackData.get("transactionId");
        String status = (String) callbackData.get("status");

        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        PaymentStatus paymentStatus;
        try {
            paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid payment status: " + status);
        }

        transaction.setStatus(paymentStatus);

        if (paymentStatus == PaymentStatus.SUCCESS) {
            transaction.setPaymentDate(LocalDateTime.now());
            transaction.setReferenceId((String) callbackData.get("referenceId"));

            // Generate receipt
            String receiptUrl = generatePaymentReceipt(transactionId);
            transaction.setReceiptUrl(receiptUrl);

            // Send payment confirmation email
            sendPaymentConfirmationEmail(transaction);

            // If this is a property-related payment, update the property status if needed
            if (transaction.getBooking() != null &&
                    (transaction.getType() == TransactionType.SECURITY_DEPOSIT ||
                            transaction.getType() == TransactionType.RENT_PAYMENT)) {
                updatePropertyStatusAfterPayment(transaction.getBooking().getProperty(), transaction.getType());
            }
        } else if (paymentStatus == PaymentStatus.FAILED) {
            transaction.setFailureReason((String) callbackData.get("failureReason"));
        }

        transaction.setPaymentGatewayResponse(callbackData.toString());

        Transaction updatedTransaction = transactionRepository.save(transaction);

        return mapToDTO(updatedTransaction);
    }

    @Override
    @Transactional
    public TransactionDTO initiateRefund(String transactionId, BigDecimal amount, String reason) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        // Security check: only the property owner or admin can initiate a refund
        User currentUser = getCurrentUser();
        if (transaction.getBooking() != null) {
            if (!transaction.getBooking().getProperty().getOwner().getId().equals(currentUser.getId()) &&
                    !isAdmin(currentUser)) {
                throw new UnauthorizedException("You are not authorized to initiate a refund for this transaction");
            }
        }

        // Validate the refund conditions
        if (transaction.getStatus() != PaymentStatus.SUCCESS) {
            throw new BadRequestException("Cannot refund a transaction that is not successful");
        }

        if (!transaction.isRefundable()) {
            throw new BadRequestException("This transaction is not refundable");
        }

        if (amount.compareTo(transaction.getAmount()) > 0) {
            throw new BadRequestException("Refund amount cannot be greater than the original amount");
        }

        // Create a new refund transaction
        Transaction refundTransaction = Transaction.builder()
                .transactionId(generateUniqueTransactionId())
                .user(transaction.getUser())
                .booking(transaction.getBooking())
                .service(transaction.getService())
                .type(TransactionType.REFUND)
                .amount(amount)
                .paymentMethod(transaction.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .description("Refund for transaction " + transactionId + ": " + reason)
                .fees(BigDecimal.ZERO) // No fees for refunds
                .tax(BigDecimal.ZERO) // No tax for refunds
                .totalAmount(amount)
                .isRefundable(false)
                .build();

        Transaction savedRefundTransaction = transactionRepository.save(refundTransaction);

        // In a real system, you would integrate with a payment gateway here
        // to initiate the refund process

        return mapToDTO(savedRefundTransaction);
    }

    @Override
    @Transactional
    public TransactionDTO updateStatus(String transactionId, PaymentStatus status, String statusDetails) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        // Security check: only admin can manually update transaction status
        User currentUser = getCurrentUser();
        if (!isAdmin(currentUser)) {
            throw new UnauthorizedException("You are not authorized to update the status of this transaction");
        }

        transaction.setStatus(status);

        if (status == PaymentStatus.SUCCESS) {
            transaction.setPaymentDate(LocalDateTime.now());

            // Generate receipt if not already present
            if (transaction.getReceiptUrl() == null) {
                String receiptUrl = generatePaymentReceipt(transactionId);
                transaction.setReceiptUrl(receiptUrl);
            }

            // Send payment confirmation email
            sendPaymentConfirmationEmail(transaction);

            // If this is a property-related payment, update the property status if needed
            if (transaction.getBooking() != null &&
                    (transaction.getType() == TransactionType.SECURITY_DEPOSIT ||
                            transaction.getType() == TransactionType.RENT_PAYMENT)) {
                updatePropertyStatusAfterPayment(transaction.getBooking().getProperty(), transaction.getType());
            }
        } else if (status == PaymentStatus.FAILED) {
            transaction.setFailureReason(statusDetails);
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);

        return mapToDTO(updatedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public String generatePaymentReceipt(String transactionId) {
        // In a real system, you would generate a PDF receipt and store it
        // For demo purposes, we'll just return a URL
        return "/api/payments/receipts/" + transactionId;
    }

//    @Override
//    @Transactional(readOnly = true)
//    public Map<String, Object> getPaymentStatistics() {
//        Map<String, Object> stats = new HashMap<>();
//
//        // Count by status
//        stats.put("totalPending", transactionRepository.countTodayTransactionsByStatus(PaymentStatus.PENDING));
//        stats.put("totalSuccess", transactionRepository.countTodayTransactionsByStatus(PaymentStatus.SUCCESS));
//        stats.put("totalFailed", transactionRepository.countTodayTransactionsByStatus(PaymentStatus.FAILED));
//
//        // Sum successful transactions
//        BigDecimal totalAmount = transactionRepository.sumTodaySuccessfulTransactions().orElse(BigDecimal.ZERO);
//        stats.put("totalAmount", totalAmount);
//
//        // Count by type
//        stats.put("securityDeposits", transactionRepository.countByTypeAndStatus(TransactionType.SECURITY_DEPOSIT, PaymentStatus.SUCCESS));
//        stats.put("rentPayments", transactionRepository.countByTypeAndStatus(TransactionType.RENT_PAYMENT, PaymentStatus.SUCCESS));
//        stats.put("brokerages", transactionRepository.countByTypeAndStatus(TransactionType.BROKERAGE, PaymentStatus.SUCCESS));
//        stats.put("serviceCharges", transactionRepository.countByTypeAndStatus(TransactionType.SERVICE_CHARGE, PaymentStatus.SUCCESS));
//
//        return stats;
//    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentStatisticsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Map<String, Object> stats = new HashMap<>();

        // Get transactions in the date range
        List<Transaction> transactions = transactionRepository.findByStatusAndDateRange(
                PaymentStatus.SUCCESS.name(), startDateTime, endDateTime);

        // Calculate total amount
        BigDecimal totalAmount = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.put("totalAmount", totalAmount);
        stats.put("transactionCount", transactions.size());

        // Group by type
        Map<TransactionType, List<Transaction>> byType = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getType));

        Map<String, BigDecimal> amountByType = new HashMap<>();
        for (Map.Entry<TransactionType, List<Transaction>> entry : byType.entrySet()) {
            BigDecimal typeAmount = entry.getValue().stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            amountByType.put(entry.getKey().name(), typeAmount);
        }

        stats.put("amountByType", amountByType);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMonthlyRevenueData(int year) {
        List<Map<String, Object>> monthlyData = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            Map<String, Object> monthData = new HashMap<>();
            String monthName = Month.of(month).name();

            // Security deposits
            BigDecimal securityDeposits = transactionRepository.sumSuccessfulTransactionsByTypeAndMonth(
                    TransactionType.SECURITY_DEPOSIT.name(), year, month).orElse(BigDecimal.ZERO);

            // Rent payments
            BigDecimal rentPayments = transactionRepository.sumSuccessfulTransactionsByTypeAndMonth(
                    TransactionType.RENT_PAYMENT.name(), year, month).orElse(BigDecimal.ZERO);

            // Brokerages
            BigDecimal brokerages = transactionRepository.sumSuccessfulTransactionsByTypeAndMonth(
                    TransactionType.BROKERAGE.name(), year, month).orElse(BigDecimal.ZERO);

            // Service charges
            BigDecimal serviceCharges = transactionRepository.sumSuccessfulTransactionsByTypeAndMonth(
                    TransactionType.SERVICE_CHARGE.name(), year, month).orElse(BigDecimal.ZERO);

            // Total
            BigDecimal total = securityDeposits.add(rentPayments).add(brokerages).add(serviceCharges);

            monthData.put("month", monthName);
            monthData.put("securityDeposits", securityDeposits);
            monthData.put("rentPayments", rentPayments);
            monthData.put("brokerages", brokerages);
            monthData.put("serviceCharges", serviceCharges);
            monthData.put("total", total);

            monthlyData.add(monthData);
        }

        return monthlyData;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        // Sum all successful transactions
        BigDecimal totalRevenue = BigDecimal.ZERO;

        // For each transaction type, sum the successful transactions
        for (TransactionType type : TransactionType.values()) {
            if (type != TransactionType.REFUND) { // Exclude refunds
                BigDecimal typeRevenue = getTotalRevenueByType(type);
                totalRevenue = totalRevenue.add(typeRevenue);
            }
        }

        return totalRevenue;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenueByType(TransactionType type) {
        // Sum all successful transactions of the specified type
        return transactionRepository.sumSuccessfulTransactionsByTypeAndDateRange(
                        type.name(), LocalDateTime.of(2000, 1, 1, 0, 0), LocalDateTime.now())
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public long countSuccessfulTransactionsToday() {
        return transactionRepository.countTodayTransactionsByStatus(PaymentStatus.SUCCESS.name());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumSuccessfulTransactionsToday() {
        return transactionRepository.sumTodaySuccessfulTransactions().orElse(BigDecimal.ZERO);
    }

    private boolean isAuthorizedToAccessTransaction(Transaction transaction) {
        User currentUser = getCurrentUser();

        // Admin can access all transactions
        if (isAdmin(currentUser)) {
            return true;
        }

        // User can access their own transactions
        if (transaction.getUser().getId().equals(currentUser.getId())) {
            return true;
        }

        // Property owner can access transactions related to their properties
        if (transaction.getBooking() != null &&
                transaction.getBooking().getProperty().getOwner().getId().equals(currentUser.getId())) {
            return true;
        }

        return false;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    private boolean isAdmin(User user) {
        return user.getRole() == UserRole.ADMIN;
    }

    private String generateUniqueTransactionId() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private BigDecimal calculateFees(BigDecimal amount, TransactionType type) {
        // This is a simplified fee calculation
        // In a real system, you would have a more complex fee structure
        switch (type) {
            case SECURITY_DEPOSIT:
            case RENT_PAYMENT:
                return amount.multiply(new BigDecimal("0.01")); // 1% fee
            case BROKERAGE:
                return amount.multiply(new BigDecimal("0.02")); // 2% fee
            case SERVICE_CHARGE:
                return amount.multiply(new BigDecimal("0.03")); // 3% fee
            default:
                return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateTax(BigDecimal amount, TransactionType type) {
        // This is a simplified tax calculation
        // In a real system, you would have a more complex tax structure
        switch (type) {
            case SECURITY_DEPOSIT:
            case RENT_PAYMENT:
                return amount.multiply(new BigDecimal("0.05")); // 5% tax
            case BROKERAGE:
                return amount.multiply(new BigDecimal("0.18")); // 18% tax
            case SERVICE_CHARGE:
                return amount.multiply(new BigDecimal("0.18")); // 18% tax
            default:
                return BigDecimal.ZERO;
        }
    }

    private void sendPaymentConfirmationEmail(Transaction transaction) {
        // Send payment confirmation email to the user
        String userName = transaction.getUser().getFirstName() + " " + transaction.getUser().getLastName();
        String amount = transaction.getTotalAmount().toString();
        String transactionIdStr = transaction.getTransactionId();
        String receiptUrl = transaction.getReceiptUrl();

        String propertyTitle = "";
        if (transaction.getBooking() != null && transaction.getBooking().getProperty() != null) {
            propertyTitle = transaction.getBooking().getProperty().getTitle();
        } else if (transaction.getService() != null) {
            propertyTitle = transaction.getService().getName();
        }

        emailService.sendPaymentConfirmationEmail(
                transaction.getUser().getEmail(),
                userName,
                propertyTitle,
                amount,
                transactionIdStr,
                receiptUrl
        );
    }

    private void updatePropertyStatusAfterPayment(Property property, TransactionType type) {
        // Update property status based on payment type
        // For example, if security deposit is paid, mark the property as rented
        if (type == TransactionType.SECURITY_DEPOSIT) {
            property.setStatus(ListingStatus.RENTED);
            propertyRepository.save(property);
        }
    }

    private TransactionDTO mapToDTO(Transaction transaction) {
        TransactionDTO dto = TransactionDTO.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .userId(transaction.getUser().getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .paymentMethod(transaction.getPaymentMethod())
                .status(transaction.getStatus())
                .paymentDate(transaction.getPaymentDate())
                .referenceId(transaction.getReferenceId())
                .failureReason(transaction.getFailureReason())
                .description(transaction.getDescription())
                .fees(transaction.getFees())
                .tax(transaction.getTax())
                .totalAmount(transaction.getTotalAmount())
                .isRefundable(transaction.isRefundable())
                .receiptUrl(transaction.getReceiptUrl())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();

        // Map user details
        UserDTO userDTO = UserDTO.builder()
                .id(transaction.getUser().getId())
                .firstName(transaction.getUser().getFirstName())
                .lastName(transaction.getUser().getLastName())
                .email(transaction.getUser().getEmail())
                .phoneNumber(transaction.getUser().getPhoneNumber())
                .profileImageUrl(transaction.getUser().getProfileImageUrl())
                .build();

        dto.setUser(userDTO);

        // Map booking details if present
        if (transaction.getBooking() != null) {
            dto.setBookingId(transaction.getBooking().getId());

            BookingDTO bookingDTO = BookingDTO.builder()
                    .id(transaction.getBooking().getId())
                    .bookingType(transaction.getBooking().getBookingType())
                    .status(transaction.getBooking().getStatus())
                    .scheduledTime(transaction.getBooking().getScheduledTime())
                    .build();

            // Map property details
            PropertyDTO propertyDTO = PropertyDTO.builder()
                    .id(transaction.getBooking().getProperty().getId())
                    .title(transaction.getBooking().getProperty().getTitle())
                    .propertyType(transaction.getBooking().getProperty().getPropertyType())
                    .bhkType(transaction.getBooking().getProperty().getBhkType())
                    .rentAmount(transaction.getBooking().getProperty().getRentAmount())
                    .city(transaction.getBooking().getProperty().getCity())
                    .locality(transaction.getBooking().getProperty().getLocality())
                    .build();

            bookingDTO.setProperty(propertyDTO);
            dto.setBooking(bookingDTO);
        }

        // Map service details if present
        if (transaction.getService() != null) {
            dto.setServiceId(transaction.getService().getId());

            ServiceDTO serviceDTO = ServiceDTO.builder()
                    .id(transaction.getService().getId())
                    .name(transaction.getService().getName())
                    .category(transaction.getService().getCategory())
                    .basePrice(transaction.getService().getBasePrice())
                    .build();

            dto.setService(serviceDTO);
        }

        return dto;
    }
}