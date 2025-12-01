// User & Authentication Types
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  role: 'CUSTOMER' | 'ADMIN';
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  kycStatus: 'PENDING' | 'VERIFIED' | 'REJECTED';
  mfaEnabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  phoneNumber: string;
}

export interface AuthResponse {
  user: User;
  token: string;
  refreshToken: string;
  mfaRequired?: boolean;
  mfaToken?: string;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  mfaRequired: boolean;
  mfaToken: string | null;
}

// Account Types
export interface Account {
  id: string;
  accountNumber: string;
  accountType: 'SAVINGS' | 'CURRENT' | 'FIXED_DEPOSIT' | 'RECURRING_DEPOSIT';
  balance: number;
  availableBalance: number;
  currency: string;
  status: 'ACTIVE' | 'INACTIVE' | 'BLOCKED' | 'DORMANT';
  branchCode: string;
  ifscCode: string;
  createdAt: string;
  updatedAt: string;
}

export interface AccountState {
  accounts: Account[];
  selectedAccount: Account | null;
  recentTransactions: Transaction[];
  isLoading: boolean;
  error: string | null;
}

// Transaction Types
export interface Transaction {
  id: string;
  transactionId: string;
  type: 'CREDIT' | 'DEBIT';
  category: 'TRANSFER' | 'BILL_PAYMENT' | 'UPI' | 'CARD' | 'ATM' | 'INTEREST' | 'FEE';
  amount: number;
  currency: string;
  fromAccount: string;
  toAccount: string;
  description: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';
  reference: string;
  createdAt: string;
  completedAt?: string;
}

export interface TransferRequest {
  fromAccountId: string;
  toAccountId?: string;
  beneficiaryId?: string;
  amount: number;
  currency: string;
  description: string;
  transferType: 'INTERNAL' | 'IMPS' | 'NEFT' | 'RTGS';
}

export interface TransferResponse {
  transactionId: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED';
  message: string;
  reference: string;
}

export interface TransactionState {
  transactions: Transaction[];
  pendingTransaction: TransferResponse | null;
  transferResult: TransferResponse | null;
  isLoading: boolean;
  error: string | null;
  totalPages: number;
  currentPage: number;
}

// Beneficiary Types
export interface Beneficiary {
  id: string;
  nickname: string;
  accountNumber: string;
  accountHolderName: string;
  bankName: string;
  ifscCode: string;
  status: 'PENDING' | 'VERIFIED' | 'REJECTED';
  transferLimit: number;
  createdAt: string;
  verifiedAt?: string;
}

export interface AddBeneficiaryRequest {
  nickname: string;
  accountNumber: string;
  confirmAccountNumber: string;
  accountHolderName: string;
  bankName: string;
  ifscCode: string;
  transferLimit?: number;
}

export interface BeneficiaryState {
  beneficiaries: Beneficiary[];
  selectedBeneficiary: Beneficiary | null;
  isLoading: boolean;
  error: string | null;
}

// Card Types
export interface Card {
  id: string;
  cardNumber: string; // Masked: **** **** **** 1234
  cardType: 'DEBIT' | 'CREDIT';
  cardNetwork: 'VISA' | 'MASTERCARD' | 'RUPAY';
  cardHolderName: string;
  expiryDate: string;
  status: 'ACTIVE' | 'BLOCKED' | 'EXPIRED' | 'CANCELLED';
  isLocked: boolean;
  linkedAccountId: string;
  dailyLimit: number;
  monthlyLimit: number;
  usedDailyLimit: number;
  usedMonthlyLimit: number;
  internationalEnabled: boolean;
  onlineEnabled: boolean;
  contactlessEnabled: boolean;
  createdAt: string;
}

export interface CardLimitUpdate {
  dailyLimit?: number;
  monthlyLimit?: number;
  internationalEnabled?: boolean;
  onlineEnabled?: boolean;
  contactlessEnabled?: boolean;
}

export interface CardState {
  cards: Card[];
  selectedCard: Card | null;
  isLoading: boolean;
  error: string | null;
}

// Payment Types
export interface BillPayment {
  id: string;
  billerId: string;
  billerName: string;
  billerCategory: string;
  consumerNumber: string;
  amount: number;
  status: 'PENDING' | 'COMPLETED' | 'FAILED';
  paidAt?: string;
}

export interface BillPaymentRequest {
  billerId: string;
  consumerNumber: string;
  amount: number;
  fromAccountId: string;
}

export interface UPIPaymentRequest {
  upiId: string;
  amount: number;
  fromAccountId: string;
  description?: string;
}

// Loan Types
export interface Loan {
  id: string;
  loanType: 'PERSONAL' | 'HOME' | 'CAR' | 'EDUCATION' | 'BUSINESS';
  principalAmount: number;
  outstandingAmount: number;
  interestRate: number;
  tenure: number; // months
  emiAmount: number;
  nextEmiDate: string;
  status: 'ACTIVE' | 'CLOSED' | 'DEFAULTED';
  disbursedAt: string;
}

export interface LoanApplication {
  loanType: 'PERSONAL' | 'HOME' | 'CAR' | 'EDUCATION' | 'BUSINESS';
  amount: number;
  tenure: number;
  purpose: string;
  employmentType: string;
  monthlyIncome: number;
}

export interface EMISchedule {
  installmentNumber: number;
  dueDate: string;
  emiAmount: number;
  principalComponent: number;
  interestComponent: number;
  balance: number;
  status: 'PAID' | 'PENDING' | 'OVERDUE';
}

// Investment Types
export interface FixedDeposit {
  id: string;
  principal: number;
  interestRate: number;
  tenure: number; // days
  maturityAmount: number;
  maturityDate: string;
  status: 'ACTIVE' | 'MATURED' | 'PREMATURE_CLOSED';
  createdAt: string;
}

export interface MutualFund {
  id: string;
  fundName: string;
  fundHouse: string;
  folioNumber: string;
  units: number;
  nav: number;
  currentValue: number;
  investedAmount: number;
  returns: number;
  returnsPercentage: number;
}

// Notification Types
export interface Notification {
  id: string;
  type: 'TRANSACTION' | 'SECURITY' | 'PROMOTIONAL' | 'SYSTEM';
  title: string;
  message: string;
  read: boolean;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  createdAt: string;
  data?: Record<string, any>;
}

export interface NotificationState {
  notifications: Notification[];
  unreadCount: number;
  isLoading: boolean;
  error: string | null;
}

// KYC Types
export interface KYCDocument {
  id: string;
  type: 'AADHAAR' | 'PAN' | 'PASSPORT' | 'DRIVING_LICENSE' | 'VOTER_ID';
  documentNumber: string;
  status: 'PENDING' | 'VERIFIED' | 'REJECTED';
  uploadedAt: string;
  verifiedAt?: string;
  rejectionReason?: string;
}

export interface KYCStatus {
  status: 'PENDING' | 'IN_PROGRESS' | 'VERIFIED' | 'REJECTED';
  documents: KYCDocument[];
  videoKycStatus?: 'PENDING' | 'COMPLETED' | 'FAILED';
  lastUpdated: string;
}

// API Response Types
export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, string[]>;
  timestamp: string;
  path: string;
}

// Support Types
export interface SupportTicket {
  id: string;
  subject: string;
  description: string;
  category: string;
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  createdAt: string;
  updatedAt: string;
  responses: SupportResponse[];
}

export interface SupportResponse {
  id: string;
  message: string;
  isStaffResponse: boolean;
  createdAt: string;
}

export interface FAQItem {
  id: string;
  question: string;
  answer: string;
  category: string;
}
