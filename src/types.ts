export interface CreditCard {
  id: number;
  name: string;
  bank: string;
  cutoffDay: number;
  paymentDueDay: number;
  creditLimit: number;
  primaryColorHex: string;
  secondaryColorHex: string;
  last4Digits: string;
  network: 'Visa' | 'Mastercard' | 'Amex' | 'Carnet' | 'Departamental' | 'American Express';
  isActive: boolean;
  isDepartmental: boolean;
  graceDays: number;
  cardholderName: string;
  annualInterestRatePercent: number;
  annualFee?: number;
  colorHex?: string;
}

export interface Expense {
  id: number;
  cardId: number;
  concept: string;
  amount: number;
  dateMillis: number;
  beneficiary: string; // "Personal", "Memé", "Ale", "Poncho", "Familiar", "Otro"
  category: string; // "Despensa", "Servicios", "Tecnología", "Gasolina", "Restaurantes", "Salud", "Hogar", "Otros"
  isMsi: boolean;
  msiTotalMonths: number;
  msiCurrentInstallment: number;
  msiTotalPurchaseAmount: number;
  notes: string;
  targetStatementMonth: string; // e.g. "2026-09" or "Septiembre 2026"
  isSubscription?: boolean;
  subscriptionId?: number | null;
}

export interface Payment {
  id: number;
  cardId: number;
  concept: string;
  amount: number;
  dateMillis: number;
  sourcePayer: string; // "Personal", "Memé", "Ale", "Poncho", "Banco"
  targetStatementMonth: string;
  notes: string;
}

export interface Subscription {
  id: number;
  name: string;
  cardId: number;
  billingDayOfMonth: number;
  totalMonthlyAmount: number;
  category: string;
  startMonth: string;
  isActive: boolean;
  notes: string;
  participantsSummary: string;
  periodicity: 'MENSUAL' | 'BIMESTRAL' | 'TRIMESTRAL' | 'SEMESTRAL' | 'ANUAL';
}

export interface FuelEntry {
  id: number;
  cardId: number;
  kmDriven: number;
  fuelType: 'Premium (Roja)' | 'Regular (Verde)';
  pricePerLiter: number;
  litersLoaded: number;
  totalCost: number;
  efficiencyKmPerL: number;
  isDivided: boolean;
  personalShare: number;
  dividedWith: string;
  dividedCount: number;
  dateMillis: number;
  notes: string;
}

export interface ServiceEntry {
  id: number;
  serviceType: 'AGUA' | 'LUZ' | 'GAS' | 'INTERNET' | string;
  dateMillis: number;
  amount: number;
  consumption?: number; // m³ for water, kWh for electricity, 0 for gas
  unitsConsumed?: number;
  unitName?: string;
  costPerUnit?: number;
  billingPeriod?: string;
  notes: string;
  cardId?: number;
}

export type TrafficLight = 'GREEN' | 'YELLOW' | 'RED';

export interface CardRecommendation {
  card: CreditCard;
  daysOfFinancing: number;
  nextCutoffDate: Date;
  paymentDueDate: Date;
  daysUntilCutoff: number;
  isBestOption: boolean;
  recommendationReason: string;
  trafficLight: TrafficLight;
  isPaymentShiftedByHoliday: boolean;
}

export interface CardCreditBalance {
  creditLimit: number;
  totalCreditLimit: number;
  totalOccupiedCredit: number;
  availableCredit: number;
  usedRatio: number;
  occupancyPercentage: number;
  regularDebt: number;
  totalMsiPendingBalance: number;
  currentCycleMsiCharge: number;
  totalPayments: number;
}

export interface MsiSummary {
  expense: Expense;
  cardName: string;
  card: CreditCard | undefined;
  monthlyPayment: number;
  totalPaidSoFar: number;
  remainingBalance: number;
  installmentsRemaining: number;
  progressPercent: number;
  completionDateString: string;
  willFinishInMonths: number;
  totalPurchaseAmount: number;
  isCompleted: boolean;
  isLastInstallmentPending: boolean;
  paymentDueDate: Date | null;
}

export interface CashFlowRelease {
  monthYearLabel: string;
  monthlyAmountFreed: number;
  finishingItems: string[];
}

export interface StatementSummary {
  statementMonth: string;
  cardId: number | null;
  cardName: string;
  cutoffDateString: string;
  paymentDueDateString: string;
  totalCharges: number;
  totalPayments: number;
  remainingBalance: number;
  isFullyPaid: boolean;
  beneficiaryBreakdown: Record<string, number>;
}

export enum InstallmentStatus {
  PAID = 'PAID',
  CURRENT = 'CURRENT',
  OVERDUE = 'OVERDUE',
  PENDING = 'PENDING',
}

export interface AmortizationRow {
  installmentNumber: number;
  monthYearLabel: string;
  paymentAmount: number;
  remainingBalanceAfter: number;
  status: InstallmentStatus;
}

export interface AmortizationMonth {
  month: number;
  startBalance: number;
  interest: number;
  iva: number;
  minimumPayment: number;
  principalPaid: number;
  endBalance: number;
}

export interface MinimumPaymentSimulationResult {
  balance: number;
  annualInterestRate: number;
  firstMonthMinimumPayment: number;
  monthsToPayOff: number;
  totalInterestPaid: number;
  totalIvaPaid: number;
  totalAmountPaidWithMinimums: number;
  savingsPayingInFull: number;
  monthlyAmortizationSample: AmortizationMonth[];
}

export interface UserProfile {
  name: string;
  email: string;
  currency: string;
}
