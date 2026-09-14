import {
  CreditCard,
  Expense,
  Payment,
  CardRecommendation,
  CardCreditBalance,
  MsiSummary,
  CashFlowRelease,
  StatementSummary,
  TrafficLight,
  MinimumPaymentSimulationResult,
  AmortizationMonth,
} from '../types.ts';

export const SPANISH_MONTHS = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
];

export function getEasterDate(year: number): { month: number; day: number } {
  const a = year % 19;
  const b = Math.floor(year / 100);
  const c = year % 100;
  const d = Math.floor(b / 4);
  const e = b % 4;
  const f = Math.floor((b + 8) / 25);
  const g = Math.floor((b - f + 1) / 3);
  const h = (19 * a + b - d - g + 15) % 30;
  const i = Math.floor(c / 4);
  const k = c % 4;
  const l = (32 + 2 * e + 2 * i - h - k) % 7;
  const m = Math.floor((a + 11 * h + 22 * l) / 451);
  const month = Math.floor((h + l - 7 * m + 114) / 31) - 1; // 0-indexed (March=2, April=3)
  const day = ((h + l - 7 * m + 114) % 31) + 1;
  return { month, day };
}

export function isMexicanBankingHoliday(date: Date): boolean {
  const dayOfWeek = date.getDay(); // 0 = Sunday, 6 = Saturday
  if (dayOfWeek === 0 || dayOfWeek === 6) return true;

  const month = date.getMonth(); // 0-11
  const day = date.getDate();
  const year = date.getFullYear();

  // 1 de enero: Año Nuevo
  if (month === 0 && day === 1) return true;

  // Primer lunes de febrero (Día de la Constitución)
  if (month === 1 && dayOfWeek === 1 && day <= 7) return true;

  // Tercer lunes de marzo (Natalicio de Benito Juárez)
  if (month === 2 && dayOfWeek === 1 && day >= 15 && day <= 21) return true;

  // Jueves Santo y Viernes Santo
  const easter = getEasterDate(year);
  const easterTime = new Date(year, easter.month, easter.day).getTime();
  const goodFridayTime = easterTime - 2 * 24 * 60 * 60 * 1000;
  const holyThursdayTime = easterTime - 3 * 24 * 60 * 60 * 1000;
  const goodFriday = new Date(goodFridayTime);
  const holyThursday = new Date(holyThursdayTime);

  if (month === holyThursday.getMonth() && day === holyThursday.getDate()) return true;
  if (month === goodFriday.getMonth() && day === goodFriday.getDate()) return true;

  // 1 de mayo: Día del Trabajo
  if (month === 4 && day === 1) return true;

  // 16 de septiembre: Día de la Independencia
  if (month === 8 && day === 16) return true;

  // 1 de octubre: Transmisión del Poder Ejecutivo Federal (2024, 2030, etc.)
  if (month === 9 && day === 1 && (year - 2024) % 6 === 0) return true;

  // 2 de noviembre: Día de Muertos
  if (month === 10 && day === 2) return true;

  // Tercer lunes de noviembre (Revolución Mexicana)
  if (month === 10 && dayOfWeek === 1 && day >= 15 && day <= 21) return true;

  // 12 de diciembre: Día del Empleado Bancario
  if (month === 11 && day === 12) return true;

  // 25 de diciembre: Navidad
  if (month === 11 && day === 25) return true;

  return false;
}

export function getEffectivePaymentDueDate(rawDueDate: Date): { date: Date; shifted: boolean } {
  const current = new Date(rawDueDate);
  let shifted = false;
  while (isMexicanBankingHoliday(current)) {
    current.setDate(current.getDate() + 1);
    shifted = true;
  }
  return { date: current, shifted };
}

export function calculateCycleDatesDetailed(
  card: CreditCard,
  referenceDate: Date = new Date()
): { cutoffDate: Date; paymentDueDate: Date; wasShifted: boolean } {
  const ref = new Date(referenceDate);
  const currentDay = ref.getDate();
  const currentMonth = ref.getMonth();
  const currentYear = ref.getFullYear();

  const cutoffCal = new Date(currentYear, currentMonth, 1, 23, 59, 59, 999);

  if (currentDay <= card.cutoffDay) {
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    const actualCutoffDay = Math.min(card.cutoffDay, daysInMonth);
    cutoffCal.setDate(actualCutoffDay);
  } else {
    const nextMonth = currentMonth + 1;
    const daysInNextMonth = new Date(currentYear, nextMonth + 1, 0).getDate();
    const actualCutoffDay = Math.min(card.cutoffDay, daysInNextMonth);
    cutoffCal.setMonth(nextMonth);
    cutoffCal.setDate(actualCutoffDay);
  }

  const cutoffDate = new Date(cutoffCal);

  const paymentCal = new Date(cutoffDate);
  if (card.paymentDueDay > card.cutoffDay) {
    const daysInMonth = new Date(paymentCal.getFullYear(), paymentCal.getMonth() + 1, 0).getDate();
    paymentCal.setDate(Math.min(card.paymentDueDay, daysInMonth));
  } else {
    paymentCal.setMonth(paymentCal.getMonth() + 1);
    const daysInMonth = new Date(paymentCal.getFullYear(), paymentCal.getMonth() + 1, 0).getDate();
    paymentCal.setDate(Math.min(card.paymentDueDay, daysInMonth));
  }

  const { date: effectiveDueDate, shifted } = getEffectivePaymentDueDate(paymentCal);
  return { cutoffDate, paymentDueDate: effectiveDueDate, wasShifted: shifted };
}

export function evaluateCardsForPurchase(
  cards: CreditCard[],
  purchaseDate: Date = new Date()
): CardRecommendation[] {
  const activeCards = cards.filter(c => c.isActive);
  if (activeCards.length === 0) return [];

  const raw = activeCards.map(card => {
    const { cutoffDate, paymentDueDate, wasShifted } = calculateCycleDatesDetailed(card, purchaseDate);

    const diffMillis = paymentDueDate.getTime() - purchaseDate.getTime();
    const daysOfFinancing = Math.max(1, Math.round(diffMillis / (1000 * 60 * 60 * 24)));

    const diffCutoff = cutoffDate.getTime() - purchaseDate.getTime();
    const daysUntilCutoff = Math.max(0, Math.round(diffCutoff / (1000 * 60 * 60 * 24)));

    let trafficLight: TrafficLight = 'YELLOW';
    if (daysOfFinancing >= 38) trafficLight = 'GREEN';
    else if (daysOfFinancing <= 22) trafficLight = 'RED';

    let reason = '';
    if (card.isDepartmental) {
      reason = `Tarjeta departamental (${card.bank}) • ${daysOfFinancing} días de financiamiento`;
    } else if (trafficLight === 'GREEN') {
      reason = `¡Excelente opción! Ganas ${daysOfFinancing} días de financiamiento libre de intereses`;
    } else if (trafficLight === 'YELLOW') {
      reason = `Opción moderada (${daysOfFinancing} días de financiamiento)`;
    } else {
      reason = `Poco financiamiento (${daysOfFinancing} días hasta la fecha límite de pago)`;
    }

    return {
      card,
      daysOfFinancing,
      nextCutoffDate: cutoffDate,
      paymentDueDate,
      daysUntilCutoff,
      isBestOption: false,
      recommendationReason: reason,
      trafficLight,
      isPaymentShiftedByHoliday: wasShifted,
    };
  });

  // Departmental cards are NEVER chosen as best option (Requirement 2 & 10)
  const bestBanking = raw
    .filter(r => !r.card.isDepartmental)
    .sort((a, b) => b.daysOfFinancing - a.daysOfFinancing)[0];

  const bestId = bestBanking?.card.id;

  const sorted = [...raw].sort((a, b) => {
    if (a.card.isDepartmental !== b.card.isDepartmental) {
      return a.card.isDepartmental ? 1 : -1;
    }
    return b.daysOfFinancing - a.daysOfFinancing;
  });

  return sorted.map(r => ({
    ...r,
    isBestOption: r.card.id === bestId,
    trafficLight: r.card.id === bestId ? 'GREEN' : r.trafficLight,
  }));
}

export function calculateCardCreditBalance(
  card: CreditCard,
  expenses: Expense[],
  payments: Payment[]
): CardCreditBalance {
  const cardExpenses = expenses.filter(e => e.cardId === card.id);
  const cardPayments = payments.filter(p => p.cardId === card.id).reduce((sum, p) => sum + p.amount, 0);

  const regularExpenses = cardExpenses.filter(e => !e.isMsi).reduce((sum, e) => sum + e.amount, 0);

  let totalMsiPendingDebt = 0;
  let currentCycleMsiCharge = 0;

  cardExpenses.filter(e => e.isMsi).forEach(msi => {
    const totalMonths = Math.max(1, msi.msiTotalMonths);
    const currentInst = Math.min(Math.max(1, msi.msiCurrentInstallment), totalMonths);
    const monthlyAmount = msi.amount > 0 ? msi.amount : (msi.msiTotalPurchaseAmount > 0 ? msi.msiTotalPurchaseAmount / totalMonths : 0);
    const totalCost = msi.msiTotalPurchaseAmount > 0 ? msi.msiTotalPurchaseAmount : monthlyAmount * totalMonths;

    const remainingFuture = Math.max(0, totalMonths - currentInst);
    const msiDebt = Math.min(totalCost, (monthlyAmount * remainingFuture) + monthlyAmount);

    totalMsiPendingDebt += msiDebt;
    currentCycleMsiCharge += monthlyAmount;
  });

  const totalOccupiedCredit = Math.max(0, (regularExpenses + totalMsiPendingDebt) - cardPayments);
  const availableCredit = Math.max(0, card.creditLimit - totalOccupiedCredit);
  const usedRatio = card.creditLimit > 0 ? Math.min(1, totalOccupiedCredit / card.creditLimit) : 0;
  const occupancyPercentage = card.creditLimit > 0 ? Math.min(100, Math.round(usedRatio * 100)) : 0;

  return {
    creditLimit: card.creditLimit,
    totalCreditLimit: card.creditLimit,
    totalOccupiedCredit,
    availableCredit,
    usedRatio,
    occupancyPercentage,
    regularDebt: Math.max(0, regularExpenses - cardPayments),
    totalMsiPendingBalance: totalMsiPendingDebt,
    currentCycleMsiCharge,
    totalPayments: cardPayments,
  };
}

export function normalizeMonth(raw: string): string {
  if (!raw) return '';
  const trimmed = raw.trim().toLowerCase();
  for (let i = 0; i < SPANISH_MONTHS.length; i++) {
    if (trimmed.includes(SPANISH_MONTHS[i].toLowerCase())) {
      return SPANISH_MONTHS[i];
    }
  }
  const match = trimmed.match(/\b\d{4}-(\d{2})\b/);
  if (match) {
    const monthNum = parseInt(match[1], 10);
    if (monthNum >= 1 && monthNum <= 12) {
      return SPANISH_MONTHS[monthNum - 1];
    }
  }
  return raw;
}

export function getMsiBaseCalendar(expense: Expense): Date {
  const raw = (expense.targetStatementMonth || '').trim();
  const yearMatch = raw.match(/\b(20\d\d)\b/);
  const currentYear = new Date().getFullYear();
  const year = yearMatch ? parseInt(yearMatch[1], 10) : currentYear;

  let monthIdx = 8; // September default
  for (let i = 0; i < SPANISH_MONTHS.length; i++) {
    if (raw.toLowerCase().includes(SPANISH_MONTHS[i].toLowerCase())) {
      monthIdx = i;
      break;
    }
  }
  if (raw.includes('-')) {
    const parts = raw.split('-');
    if (parts.length >= 2) {
      const parsedMonth = parseInt(parts[1], 10);
      if (!isNaN(parsedMonth) && parsedMonth >= 1 && parsedMonth <= 12) {
        monthIdx = parsedMonth - 1;
      }
    }
  }

  return new Date(year, monthIdx, 1, 12, 0, 0);
}

export function computeMsiSummary(expense: Expense, card: CreditCard | undefined): MsiSummary {
  const totalMonths = Math.max(1, expense.msiTotalMonths);
  const currentInstallment = Math.max(1, expense.msiCurrentInstallment);
  const monthlyAmount = expense.amount;
  const totalCost = expense.msiTotalPurchaseAmount > 0 ? expense.msiTotalPurchaseAmount : monthlyAmount * totalMonths;

  const baseDate = getMsiBaseCalendar(expense);
  const completionDate = new Date(baseDate);
  const remainingInstallmentsBefore = Math.max(0, totalMonths - currentInstallment);
  completionDate.setMonth(completionDate.getMonth() + remainingInstallmentsBefore);

  const completionMonthName = SPANISH_MONTHS[completionDate.getMonth()];
  const completionDateString = `${completionMonthName} ${completionDate.getFullYear()}`;

  let isFinalDueDatePassed = false;
  let paymentDueDate: Date | null = null;

  if (card) {
    const pDate = new Date(completionDate.getFullYear(), completionDate.getMonth(), 1, 23, 59, 59);
    if (card.paymentDueDay > card.cutoffDay) {
      pDate.setDate(Math.min(card.paymentDueDay, 28));
    } else {
      pDate.setMonth(pDate.getMonth() + 1);
      pDate.setDate(Math.min(card.paymentDueDay, 28));
    }
    const eff = getEffectivePaymentDueDate(pDate);
    paymentDueDate = eff.date;
    isFinalDueDatePassed = new Date().getTime() > paymentDueDate.getTime();
  } else {
    const now = new Date();
    isFinalDueDatePassed =
      now.getFullYear() > completionDate.getFullYear() ||
      (now.getFullYear() === completionDate.getFullYear() && now.getMonth() > completionDate.getMonth());
  }

  let isCompleted = false;
  let isLastInstallmentPending = false;
  let remainingInstallments = 0;

  if (currentInstallment > totalMonths) {
    isCompleted = true;
    remainingInstallments = 0;
  } else if (currentInstallment === totalMonths) {
    if (isFinalDueDatePassed) {
      isCompleted = true;
      remainingInstallments = 0;
    } else {
      isCompleted = false;
      isLastInstallmentPending = true;
      remainingInstallments = 1;
    }
  } else {
    isCompleted = false;
    remainingInstallments = Math.max(1, totalMonths - currentInstallment);
  }

  const totalPaid = isCompleted
    ? totalCost
    : Math.min(totalCost, monthlyAmount * (totalMonths - remainingInstallments));
  const remainingBalance = Math.max(0, monthlyAmount * remainingInstallments);
  const progressPercent = isCompleted ? 1 : Math.min(0.99, (totalMonths - remainingInstallments) / totalMonths);

  return {
    expense,
    cardName: card?.name || 'TDC',
    card,
    monthlyPayment: monthlyAmount,
    totalPaidSoFar: totalPaid,
    remainingBalance,
    installmentsRemaining: remainingInstallments,
    progressPercent,
    completionDateString,
    willFinishInMonths: remainingInstallments,
    totalPurchaseAmount: totalCost,
    isCompleted,
    isLastInstallmentPending,
    paymentDueDate,
  };
}

export function projectCashFlowRelease(msiSummaries: MsiSummary[]): CashFlowRelease[] {
  const activeItems = msiSummaries.filter(m => m.installmentsRemaining > 0);
  if (activeItems.length === 0) return [];

  const grouped = new Map<string, MsiSummary[]>();
  for (const item of activeItems) {
    const label = item.completionDateString;
    const list = grouped.get(label) || [];
    list.push(item);
    grouped.set(label, list);
  }

  const result: CashFlowRelease[] = [];
  grouped.forEach((items, monthYearLabel) => {
    const freed = items.reduce((sum, i) => sum + i.monthlyPayment, 0);
    const finishingItems = items.map(
      i => `${i.expense.concept} (+$${i.monthlyPayment.toFixed(2)}/mes)`
    );
    result.push({
      monthYearLabel,
      monthlyAmountFreed: freed,
      finishingItems,
    });
  });

  return result;
}

export function computeStatementSummary(
  statementMonth: string,
  selectedCard: CreditCard | null,
  expenses: Expense[],
  payments: Payment[]
): StatementSummary {
  const filteredExpenses = selectedCard
    ? expenses.filter(e => e.cardId === selectedCard.id)
    : expenses;
  const filteredPayments = selectedCard
    ? payments.filter(p => p.cardId === selectedCard.id)
    : payments;

  const totalCharges = filteredExpenses.reduce((sum, e) => sum + e.amount, 0);
  const totalPayments = filteredPayments.reduce((sum, p) => sum + p.amount, 0);
  const remainingBalance = totalCharges - totalPayments;

  const beneficiaryBreakdown: Record<string, number> = {};
  for (const exp of filteredExpenses) {
    const ben = exp.beneficiary || 'Personal';
    beneficiaryBreakdown[ben] = (beneficiaryBreakdown[ben] || 0) + exp.amount;
  }

  const cutoffDateString = selectedCard
    ? `Día ${selectedCard.cutoffDay} del mes`
    : 'Varios cortes según tarjeta';

  const paymentDueDateString = selectedCard
    ? `Día ${selectedCard.paymentDueDay} del mes`
    : 'Vencimientos según tarjeta';

  return {
    statementMonth,
    cardId: selectedCard?.id || null,
    cardName: selectedCard?.name || 'Todas las Tarjetas (Consolidado)',
    cutoffDateString,
    paymentDueDateString,
    totalCharges,
    totalPayments,
    remainingBalance,
    isFullyPaid: remainingBalance <= 0.01,
    beneficiaryBreakdown,
  };
}

export interface CardMonthlyStatement {
  card: CreditCard;
  totalToPayToAvoidInterest: number;
  regularExpensesAmount: number;
  msiExpensesAmount: number;
  totalPaymentsAmount: number;
  netPendingToPay: number;
  expenses: Expense[];
  payments: Payment[];
}

export interface GlobalMonthlySummary {
  totalToPayToAvoidInterest: number;
  totalExpensesAmount: number;
  totalPaymentsMade: number;
  netPendingToPay: number;
  allExpensesInMonth: Expense[];
  allPaymentsInMonth: Payment[];
}

export function computeCardMonthlyStatement(
  card: CreditCard,
  expenses: Expense[],
  payments: Payment[],
  statementMonth: string
): CardMonthlyStatement {
  const normTarget = normalizeMonth(statementMonth).toLowerCase();
  const cardExpenses = expenses.filter(
    e => e.cardId === card.id && (
      !e.targetStatementMonth ||
      normalizeMonth(e.targetStatementMonth).toLowerCase() === normTarget
    )
  );
  const cardPayments = payments.filter(
    p => p.cardId === card.id && (
      !p.targetStatementMonth ||
      normalizeMonth(p.targetStatementMonth).toLowerCase() === normTarget
    )
  );

  let regular = 0;
  let msi = 0;
  cardExpenses.forEach(e => {
    if (e.isMsi) {
      msi += e.amount;
    } else {
      regular += e.amount;
    }
  });

  const paymentsTotal = cardPayments.reduce((sum, p) => sum + p.amount, 0);
  const totalToPay = regular + msi;
  const netPending = Math.max(0, totalToPay - paymentsTotal);

  return {
    card,
    totalToPayToAvoidInterest: totalToPay,
    regularExpensesAmount: regular,
    msiExpensesAmount: msi,
    totalPaymentsAmount: paymentsTotal,
    netPendingToPay: netPending,
    expenses: cardExpenses,
    payments: cardPayments,
  };
}

export function computeGlobalMonthlySummary(
  cards: CreditCard[],
  expenses: Expense[],
  payments: Payment[],
  statementMonth: string
): GlobalMonthlySummary {
  const cardStatements = cards.map(c =>
    computeCardMonthlyStatement(c, expenses, payments, statementMonth)
  );

  const totalToPayToAvoidInterest = cardStatements.reduce(
    (sum, s) => sum + s.totalToPayToAvoidInterest,
    0
  );
  const totalPaymentsMade = cardStatements.reduce(
    (sum, s) => sum + s.totalPaymentsAmount,
    0
  );
  const netPendingToPay = Math.max(0, totalToPayToAvoidInterest - totalPaymentsMade);

  const allExpensesInMonth = cardStatements.flatMap(s => s.expenses);
  const allPaymentsInMonth = cardStatements.flatMap(s => s.payments);

  return {
    totalToPayToAvoidInterest,
    totalExpensesAmount: totalToPayToAvoidInterest,
    totalPaymentsMade,
    netPendingToPay,
    allExpensesInMonth,
    allPaymentsInMonth,
  };
}

export function simulateMinimumPaymentPayoff(
  initialBalance: number,
  annualInterestRatePercent: number = 55,
  creditLimit: number = 0,
  maxMonths: number = 120
): MinimumPaymentSimulationResult {
  if (initialBalance <= 0) {
    return {
      balance: 0,
      annualInterestRate: annualInterestRatePercent,
      firstMonthMinimumPayment: 0,
      monthsToPayOff: 0,
      totalInterestPaid: 0,
      totalIvaPaid: 0,
      totalAmountPaidWithMinimums: 0,
      savingsPayingInFull: 0,
      monthlyAmortizationSample: [],
    };
  }

  const monthlyRate = (annualInterestRatePercent / 100) / 12;
  const ivaRate = 0.16;

  let currentBalance = initialBalance;
  let totalInterest = 0;
  let totalIva = 0;
  let totalPaid = 0;
  let monthCount = 0;
  let firstMonthMin = 0;

  const sampleList: AmortizationMonth[] = [];

  while (currentBalance > 1 && monthCount < maxMonths) {
    monthCount++;
    const startBal = currentBalance;
    const interest = currentBalance * monthlyRate;
    const iva = interest * ivaRate;
    const chargesThisMonth = interest + iva;

    const formula1 = (currentBalance * 0.015) + chargesThisMonth;
    const formula2 = creditLimit > 0 ? creditLimit * 0.0125 : 200;
    const minCalculated = Math.max(formula1, formula2, 200);

    const totalDue = currentBalance + chargesThisMonth;
    const payment = Math.min(minCalculated, totalDue);

    if (monthCount === 1) {
      firstMonthMin = payment;
    }

    totalInterest += interest;
    totalIva += iva;
    totalPaid += payment;

    const principalPaid = Math.max(0, payment - chargesThisMonth);
    currentBalance = Math.max(0, currentBalance - principalPaid);

    if (monthCount <= 6 || monthCount % 6 === 0 || currentBalance <= 1) {
      sampleList.push({
        month: monthCount,
        startBalance: startBal,
        interest,
        iva,
        minimumPayment: payment,
        principalPaid,
        endBalance: currentBalance,
      });
    }
  }

  if (currentBalance > 0) {
    totalPaid += currentBalance;
  }

  const savings = Math.max(0, totalPaid - initialBalance);

  return {
    balance: initialBalance,
    annualInterestRate: annualInterestRatePercent,
    firstMonthMinimumPayment: firstMonthMin,
    monthsToPayOff: monthCount,
    totalInterestPaid: totalInterest,
    totalIvaPaid: totalIva,
    totalAmountPaidWithMinimums: totalPaid,
    savingsPayingInFull: savings,
    monthlyAmortizationSample: sampleList,
  };
}

export function formatCurrency(amount: number, isPrivate: boolean = false): string {
  if (isPrivate) return '$ ••••••';
  return new Intl.NumberFormat('es-MX', {
    style: 'currency',
    currency: 'MXN',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
}

export function formatDate(millis: number): string {
  const d = new Date(millis);
  return `${d.getDate().toString().padStart(2, '0')}/${(d.getMonth() + 1).toString().padStart(2, '0')}/${d.getFullYear()}`;
}

export function formatFriendlyDate(date: Date): string {
  const day = date.getDate();
  const month = SPANISH_MONTHS[date.getMonth()];
  return `${day} de ${month}`;
}
