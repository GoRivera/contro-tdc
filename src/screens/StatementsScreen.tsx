import React, { useState, useMemo } from 'react';
import { useApp } from '../context/AppContext.tsx';
import {
  computeCardMonthlyStatement,
  computeGlobalMonthlySummary,
  formatCurrency,
  formatDate,
  SPANISH_MONTHS,
} from '../domain/CreditCardCalculator.ts';
import { Expense, Payment, MsiSummary } from '../types.ts';
import {
  ChevronLeft,
  ChevronRight,
  PieChart as PieChartIcon,
  CreditCard as CardIcon,
  ArrowDownLeft,
  Plus,
  Trash2,
  Edit2,
  Calendar,
  CheckCircle2,
  DollarSign,
  Printer,
} from 'lucide-react';
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  Tooltip,
  Legend,
} from 'recharts';

interface StatementsScreenProps {
  onOpenAddExpense: (cardId?: number) => void;
  onOpenAddPayment: (cardId?: number) => void;
  onEditExpense: (expense: Expense) => void;
  onOpenMinimumPayoff: (balance: number, rate: number, limit: number) => void;
}

const CATEGORY_COLORS: Record<string, string> = {
  Despensa: '#3b82f6',
  Servicios: '#06b6d4',
  Tecnología: '#8b5cf6',
  Gasolina: '#f59e0b',
  Restaurantes: '#ef4444',
  Salud: '#10b981',
  Hogar: '#ec4899',
  Otros: '#64748b',
};

export const StatementsScreen: React.FC<StatementsScreenProps> = ({
  onOpenAddExpense,
  onOpenAddPayment,
  onEditExpense,
  onOpenMinimumPayoff,
}) => {
  const { cards, expenses, payments, deleteExpense, deletePayment, isPrivateMode } = useApp();

  const currentYear = new Date().getFullYear();
  const currentMonthIdx = new Date().getMonth();

  // Generate selectable months (last 6 months and next 3 months)
  const availableMonths = useMemo(() => {
    const list: string[] = [];
    for (let offset = -4; offset <= 3; offset++) {
      const d = new Date(currentYear, currentMonthIdx + offset, 1);
      list.push(`${SPANISH_MONTHS[d.getMonth()]} ${d.getFullYear()}`);
    }
    return list;
  }, [currentYear, currentMonthIdx]);

  const defaultMonth = `${SPANISH_MONTHS[currentMonthIdx]} ${currentYear}`;
  const [selectedMonth, setSelectedMonth] = useState<string>(defaultMonth);
  const [selectedCardFilter, setSelectedCardFilter] = useState<number | null>(null);

  // Global summary for this month
  const globalSummary = useMemo(() => {
    return computeGlobalMonthlySummary(cards, expenses, payments, selectedMonth);
  }, [cards, expenses, payments, selectedMonth]);

  // Card statements
  const cardStatements = useMemo(() => {
    return cards
      .filter(c => selectedCardFilter === null || c.id === selectedCardFilter)
      .map(c => computeCardMonthlyStatement(c, expenses, payments, selectedMonth));
  }, [cards, expenses, payments, selectedMonth, selectedCardFilter]);

  // Donut chart category data
  const categoryData = useMemo(() => {
    const map: Record<string, number> = {};
    globalSummary.allExpensesInMonth.forEach((exp: Expense) => {
      map[exp.category] = (map[exp.category] || 0) + exp.amount;
    });

    return Object.entries(map).map(([name, value]) => ({
      name,
      value: Math.round(value * 100) / 100,
      color: CATEGORY_COLORS[name] || '#94a3b8',
    }));
  }, [globalSummary]);

  // Beneficiary distribution data
  const beneficiaryData = useMemo(() => {
    const map: Record<string, number> = {};
    globalSummary.allExpensesInMonth.forEach((exp: Expense) => {
      map[exp.beneficiary] = (map[exp.beneficiary] || 0) + exp.amount;
    });

    return Object.entries(map).map(([name, value]) => ({
      name,
      value: Math.round(value * 100) / 100,
    }));
  }, [globalSummary]);

  // Month navigation helpers
  const currentIdx = availableMonths.indexOf(selectedMonth);
  const handlePrevMonth = () => {
    if (currentIdx > 0) setSelectedMonth(availableMonths[currentIdx - 1]);
  };
  const handleNextMonth = () => {
    if (currentIdx < availableMonths.length - 1) setSelectedMonth(availableMonths[currentIdx + 1]);
  };

  return (
    <div className="space-y-6 pb-12">
      {/* Month Navigator Header */}
      <div className="flex items-center justify-between p-4 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
        <button
          onClick={handlePrevMonth}
          disabled={currentIdx <= 0}
          className="p-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-30 disabled:pointer-events-none transition-colors"
        >
          <ChevronLeft className="w-5 h-5" />
        </button>

        <div className="text-center">
          <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider block">
            Estado de Cuenta del Periodo
          </span>
          <h2 className="text-xl sm:text-2xl font-black text-slate-900 dark:text-white capitalize">
            {selectedMonth}
          </h2>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => window.print()}
            title="Imprimir o exportar PDF"
            className="p-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors hidden sm:block"
          >
            <Printer className="w-5 h-5" />
          </button>
          <button
            onClick={handleNextMonth}
            disabled={currentIdx >= availableMonths.length - 1}
            className="p-2.5 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-30 disabled:pointer-events-none transition-colors"
          >
            <ChevronRight className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* Hero Financial Summary Banner */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Total to pay to avoid interest */}
        <div className="p-6 rounded-3xl bg-gradient-to-br from-slate-900 to-indigo-950 text-white shadow-xl border border-slate-800 flex flex-col justify-between">
          <div>
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">
              Pago para no generar intereses
            </span>
            <div className="text-3xl font-black font-mono tracking-tight text-white">
              {formatCurrency(globalSummary.totalToPayToAvoidInterest, isPrivateMode)}
            </div>
          </div>
          <p className="text-xs text-slate-400 mt-4">
            Incluye compras corrientes del corte + mensualidades MSI activas
          </p>
        </div>

        {/* Payments made */}
        <div className="p-6 rounded-3xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200/80 dark:border-emerald-800/60 flex flex-col justify-between">
          <div>
            <span className="text-xs font-bold uppercase tracking-wider text-emerald-700 dark:text-emerald-400 block mb-1">
              Abonos / Pagos Registrados
            </span>
            <div className="text-3xl font-black font-mono tracking-tight text-emerald-800 dark:text-emerald-300">
              {formatCurrency(globalSummary.totalPaymentsMade, isPrivateMode)}
            </div>
          </div>
          <button
            onClick={() => onOpenAddPayment()}
            className="mt-4 text-xs font-bold text-emerald-700 dark:text-emerald-400 hover:underline flex items-center gap-1"
          >
            <ArrowDownLeft className="w-4 h-4" />
            <span>Registrar nuevo abono al periodo</span>
          </button>
        </div>

        {/* Net pending to liquidate */}
        <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between">
          <div>
            <span className="text-xs font-bold uppercase tracking-wider text-slate-500 block mb-1">
              Saldo Pendiente Neto del Mes
            </span>
            <div className="text-3xl font-black font-mono tracking-tight text-slate-900 dark:text-white">
              {formatCurrency(globalSummary.netPendingToPay, isPrivateMode)}
            </div>
          </div>
          <div className="mt-4 flex items-center justify-between text-xs">
            <span className="text-slate-500">
              {globalSummary.netPendingToPay <= 0 ? '¡Periodo liquidado!' : 'Pendiente por pagar'}
            </span>
            {globalSummary.netPendingToPay > 0 && (
              <button
                onClick={() => onOpenMinimumPayoff(globalSummary.netPendingToPay, 55, 100000)}
                className="font-bold text-indigo-600 dark:text-indigo-400 hover:underline"
              >
                Simular mínimo
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Visual Analytics Row: Category Donut & Beneficiary Distribution */}
      {categoryData.length > 0 && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Category Donut */}
          <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm">
            <div className="flex items-center gap-2 mb-4">
              <PieChartIcon className="w-5 h-5 text-blue-600 dark:text-blue-400" />
              <h3 className="text-base font-extrabold text-slate-900 dark:text-white">
                Distribución por Categorías
              </h3>
            </div>

            <div className="h-64 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categoryData}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={85}
                    paddingAngle={3}
                    dataKey="value"
                  >
                    {categoryData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip
                    formatter={(val: number) => [formatCurrency(val, isPrivateMode), 'Monto']}
                  />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Beneficiary Breakdown */}
          <div className="p-6 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm flex flex-col justify-between">
            <div>
              <h3 className="text-base font-extrabold text-slate-900 dark:text-white mb-1">
                Gasto por Beneficiario
              </h3>
              <p className="text-xs text-slate-400 mb-4">
                Suma de cargos asignados a cada persona en este periodo
              </p>

              <div className="space-y-3">
                {beneficiaryData.map(b => {
                  const percent = Math.round((b.value / (globalSummary.totalExpensesAmount || 1)) * 100);

                  return (
                    <div key={b.name}>
                      <div className="flex items-center justify-between text-xs font-semibold mb-1">
                        <span className="text-slate-700 dark:text-slate-300">{b.name}</span>
                        <span className="font-mono text-slate-900 dark:text-white">
                          {formatCurrency(b.value, isPrivateMode)} ({percent}%)
                        </span>
                      </div>
                      <div className="w-full bg-slate-100 dark:bg-slate-800 h-2 rounded-full overflow-hidden">
                        <div
                          className="bg-indigo-600 h-2 rounded-full"
                          style={{ width: `${percent}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            <div className="pt-4 mt-4 border-t border-slate-100 dark:border-slate-800 flex justify-end">
              <button
                onClick={() => onOpenAddExpense()}
                className="px-4 py-2 rounded-xl bg-blue-600 text-white font-bold text-xs shadow-sm hover:bg-blue-700 flex items-center gap-1.5"
              >
                <Plus className="w-4 h-4" />
                <span>Registrar Gasto</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Card Filter Chips */}
      <div className="flex items-center gap-1.5 overflow-x-auto py-1">
        <button
          onClick={() => setSelectedCardFilter(null)}
          className={`px-3 py-1.5 text-xs rounded-xl font-bold whitespace-nowrap transition-colors ${
            selectedCardFilter === null
              ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
              : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-200'
          }`}
        >
          Todas las Tarjetas ({cards.length})
        </button>
        {cards.map(c => (
          <button
            key={c.id}
            onClick={() => setSelectedCardFilter(c.id)}
            className={`px-3 py-1.5 text-xs rounded-xl font-bold whitespace-nowrap transition-colors ${
              selectedCardFilter === c.id
                ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
                : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-200'
            }`}
          >
            {c.name}
          </button>
        ))}
      </div>

      {/* Individual Card Statements Accordion */}
      <div className="space-y-5">
        {cardStatements.map(stmt => {
          return (
            <div
              key={stmt.card.id}
              className="rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm overflow-hidden"
            >
              {/* Card Statement Header */}
              <div className="p-5 bg-slate-50/80 dark:bg-slate-850 border-b border-slate-200 dark:border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div className="flex items-center gap-3">
                  <div className="w-11 h-11 rounded-2xl bg-blue-600/10 text-blue-600 dark:text-blue-400 flex items-center justify-center font-bold">
                    <CardIcon className="w-6 h-6" />
                  </div>
                  <div>
                    <div className="flex items-center gap-2">
                      <h4 className="text-lg font-black text-slate-900 dark:text-white">
                        {stmt.card.name}
                      </h4>
                      <span className="text-xs text-slate-500 font-semibold">
                        {stmt.card.bank} •••• {stmt.card.last4Digits}
                      </span>
                    </div>
                    <span className="text-xs text-slate-400">
                      Corte: Día {stmt.card.cutoffDay} • Límite Pago: Día {stmt.card.paymentDueDay}
                    </span>
                  </div>
                </div>

                {/* Amount to pay for this card */}
                <div className="flex items-center gap-4 sm:text-right">
                  <div>
                    <span className="text-[10px] uppercase font-bold text-slate-400 block">
                      Pago para no generar intereses
                    </span>
                    <span className="text-xl font-black font-mono text-slate-900 dark:text-white">
                      {formatCurrency(stmt.totalToPayToAvoidInterest, isPrivateMode)}
                    </span>
                  </div>
                  <button
                    onClick={() => onOpenAddPayment(stmt.card.id)}
                    className="px-3.5 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold shadow-sm transition-colors flex items-center gap-1"
                  >
                    <ArrowDownLeft className="w-4 h-4" />
                    <span>Abonar</span>
                  </button>
                </div>
              </div>

              {/* Card Statement Metrics Breakdown */}
              <div className="p-4 grid grid-cols-2 sm:grid-cols-4 gap-3 bg-white dark:bg-slate-900 border-b border-slate-100 dark:border-slate-800">
                <div className="p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800/50">
                  <span className="text-[10px] uppercase font-bold text-slate-400 block">
                    Compras Corrientes
                  </span>
                  <span className="text-sm font-bold font-mono text-slate-900 dark:text-white">
                    {formatCurrency(stmt.regularExpensesAmount, isPrivateMode)}
                  </span>
                </div>
                <div className="p-2.5 rounded-xl bg-blue-50/60 dark:bg-blue-950/30">
                  <span className="text-[10px] uppercase font-bold text-blue-600 dark:text-blue-400 block">
                    Mensualidades MSI
                  </span>
                  <span className="text-sm font-bold font-mono text-blue-700 dark:text-blue-300">
                    {formatCurrency(stmt.msiExpensesAmount, isPrivateMode)}
                  </span>
                </div>
                <div className="p-2.5 rounded-xl bg-emerald-50/60 dark:bg-emerald-950/30">
                  <span className="text-[10px] uppercase font-bold text-emerald-600 dark:text-emerald-400 block">
                    Abonos del Mes
                  </span>
                  <span className="text-sm font-bold font-mono text-emerald-700 dark:text-emerald-300">
                    {formatCurrency(stmt.totalPaymentsAmount, isPrivateMode)}
                  </span>
                </div>
                <div className="p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800/50">
                  <span className="text-[10px] uppercase font-bold text-slate-400 block">
                    Saldo Restante
                  </span>
                  <span className="text-sm font-bold font-mono text-slate-900 dark:text-white">
                    {formatCurrency(stmt.netPendingToPay, isPrivateMode)}
                  </span>
                </div>
              </div>

              {/* Transactions List */}
              <div className="p-5">
                <h5 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-3">
                  Transacciones del Periodo ({stmt.expenses.length + stmt.payments.length})
                </h5>

                {stmt.expenses.length === 0 && stmt.payments.length === 0 ? (
                  <p className="text-xs text-slate-400 italic py-2">
                    No hay movimientos registrados para esta tarjeta en {selectedMonth}.
                  </p>
                ) : (
                  <div className="divide-y divide-slate-100 dark:divide-slate-800 border border-slate-100 dark:border-slate-800 rounded-2xl overflow-hidden">
                    {/* Expenses */}
                    {stmt.expenses.map((exp: Expense) => (
                      <div
                        key={`exp-${exp.id}`}
                        className="p-3.5 flex items-center justify-between text-xs hover:bg-slate-50/50 dark:hover:bg-slate-800/40 transition-colors"
                      >
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 rounded-xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center font-bold text-slate-500">
                            {exp.isMsi ? 'MSI' : 'TDC'}
                          </div>
                          <div>
                            <div className="flex items-center gap-2">
                              <span className="font-bold text-slate-900 dark:text-white">
                                {exp.concept}
                              </span>
                              {exp.isMsi && (
                                <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-blue-100 text-blue-800 dark:bg-blue-900/50 dark:text-blue-300">
                                  {exp.msiCurrentInstallment}/{exp.msiTotalMonths} MSI
                                </span>
                              )}
                            </div>
                            <span className="text-slate-400 text-[11px]">
                              {formatDate(exp.dateMillis)} • {exp.beneficiary} • {exp.category}
                            </span>
                          </div>
                        </div>

                        <div className="flex items-center gap-3">
                          <span className="font-mono font-bold text-sm text-slate-900 dark:text-white">
                            {formatCurrency(exp.amount, isPrivateMode)}
                          </span>
                          <button
                            onClick={() => onEditExpense(exp)}
                            className="p-1 text-slate-400 hover:text-slate-700 dark:hover:text-slate-200"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => {
                              if (confirm(`¿Eliminar el gasto "${exp.concept}"?`)) {
                                deleteExpense(exp.id);
                              }
                            }}
                            className="p-1 text-slate-400 hover:text-red-500"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>
                    ))}

                    {/* Payments */}
                    {stmt.payments.map((pay: Payment) => (
                      <div
                        key={`pay-${pay.id}`}
                        className="p-3.5 flex items-center justify-between text-xs bg-emerald-50/40 dark:bg-emerald-950/20 hover:bg-emerald-50/70 transition-colors"
                      >
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 rounded-xl bg-emerald-100 dark:bg-emerald-900/50 flex items-center justify-center font-bold text-emerald-600 dark:text-emerald-300">
                            <ArrowDownLeft className="w-4 h-4" />
                          </div>
                          <div>
                            <span className="font-bold text-emerald-900 dark:text-emerald-300">
                              {pay.concept}
                            </span>
                            <span className="text-emerald-700/70 dark:text-emerald-400 text-[11px] block">
                              {formatDate(pay.dateMillis)} • Pagado por {pay.sourcePayer}
                            </span>
                          </div>
                        </div>

                        <div className="flex items-center gap-3">
                          <span className="font-mono font-bold text-sm text-emerald-700 dark:text-emerald-300">
                            -{formatCurrency(pay.amount, isPrivateMode)}
                          </span>
                          <button
                            onClick={() => {
                              if (confirm(`¿Eliminar el abono "${pay.concept}"?`)) {
                                deletePayment(pay.id);
                              }
                            }}
                            className="p-1 text-slate-400 hover:text-red-500"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
