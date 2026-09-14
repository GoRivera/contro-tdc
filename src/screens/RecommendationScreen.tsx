import React, { useState, useMemo } from 'react';
import { useApp } from '../context/AppContext.tsx';
import { evaluateCardsForPurchase, formatCurrency, formatFriendlyDate, calculateCardCreditBalance } from '../domain/CreditCardCalculator.ts';
import { CreditCardVisual } from '../components/CreditCardVisual.tsx';
import { CreditCard } from '../types.ts';
import { Sparkles, ArrowRight, ShieldCheck, AlertCircle, Plus, ArrowDownLeft, Clock, Calendar, ChevronRight } from 'lucide-react';

interface RecommendationScreenProps {
  onOpenAddExpense: (cardId?: number) => void;
  onOpenAddPayment: (cardId?: number) => void;
  onNavigateToStatements: () => void;
}

export const RecommendationScreen: React.FC<RecommendationScreenProps> = ({
  onOpenAddExpense,
  onOpenAddPayment,
  onNavigateToStatements,
}) => {
  const { cards, expenses, payments, isPrivateMode } = useApp();
  const [filterType, setFilterType] = useState<'ALL' | 'BANKING' | 'DEPARTMENTAL'>('ALL');

  const recommendations = useMemo(() => {
    return evaluateCardsForPurchase(cards, new Date());
  }, [cards]);

  const filteredRecommendations = useMemo(() => {
    if (filterType === 'BANKING') {
      return recommendations.filter(r => !r.card.isDepartmental);
    }
    if (filterType === 'DEPARTMENTAL') {
      return recommendations.filter(r => r.card.isDepartmental);
    }
    return recommendations;
  }, [recommendations, filterType]);

  const bestOption = recommendations.find(r => r.isBestOption);

  // Calculate quick cycle summary across cards
  const totalOccupied = useMemo(() => {
    return cards.reduce((acc, card) => {
      const balance = calculateCardCreditBalance(card, expenses, payments);
      return acc + balance.totalOccupiedCredit;
    }, 0);
  }, [cards, expenses, payments]);

  return (
    <div className="space-y-6 pb-12">
      {/* Month Cycle Summary Banner */}
      <div className="bg-gradient-to-br from-slate-900 via-slate-800 to-indigo-950 text-white rounded-3xl p-6 shadow-xl border border-slate-700/50 relative overflow-hidden">
        <div className="absolute top-0 right-0 w-80 h-80 bg-blue-500/10 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center gap-2 mb-1.5">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse" />
              <span className="text-xs font-bold uppercase tracking-wider text-slate-300">
                Saldo Ocupado Total en Tarjetas
              </span>
            </div>
            <div className="text-3xl sm:text-4xl font-black tracking-tight font-mono">
              {formatCurrency(totalOccupied, isPrivateMode)}
            </div>
            <p className="text-xs text-slate-400 mt-1">
              Calculado considerando compras corrientes, amortización MSI y abonos aplicados
            </p>
          </div>

          <div className="flex items-center flex-wrap gap-2.5">
            <button
              onClick={() => onOpenAddExpense()}
              className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-bold shadow-md shadow-blue-500/30 transition-colors"
            >
              <Plus className="w-4 h-4" />
              <span>Registrar Gasto</span>
            </button>
            <button
              onClick={() => onOpenAddPayment()}
              className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold shadow-md shadow-emerald-500/30 transition-colors"
            >
              <ArrowDownLeft className="w-4 h-4" />
              <span>Registrar Abono</span>
            </button>
            <button
              onClick={onNavigateToStatements}
              className="flex items-center gap-1 px-3.5 py-2.5 rounded-xl bg-white/10 hover:bg-white/15 text-slate-200 text-xs font-semibold transition-colors"
            >
              <span>Ver Estados de Cuenta</span>
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Hero Recommendation Card */}
      {bestOption && (
        <div className="bg-gradient-to-r from-emerald-500/10 via-blue-500/5 to-transparent dark:from-emerald-950/30 dark:via-blue-950/20 rounded-3xl p-5 sm:p-6 border border-emerald-500/20 dark:border-emerald-700/30">
          <div className="flex items-center gap-2 mb-3">
            <div className="px-2.5 py-1 rounded-full bg-emerald-600 text-white font-black text-xs uppercase tracking-wider flex items-center gap-1 shadow-sm">
              <Sparkles className="w-3.5 h-3.5" />
              Tarjeta Óptima Hoy
            </div>
            <span className="text-xs font-bold text-emerald-700 dark:text-emerald-400">
              {bestOption.daysOfFinancing} días de financiamiento libre de intereses
            </span>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
            <div className="lg:col-span-5">
              <CreditCardVisual card={bestOption.card} />
            </div>

            <div className="lg:col-span-7 space-y-4">
              <div>
                <h3 className="text-xl font-black text-slate-900 dark:text-white">
                  {bestOption.card.name} ({bestOption.card.bank})
                </h3>
                <p className="text-sm font-medium text-slate-600 dark:text-slate-300 mt-0.5">
                  {bestOption.recommendationReason}
                </p>
              </div>

              {/* Cycle Milestones */}
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
                <div className="p-3 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 shadow-sm">
                  <span className="text-[10px] uppercase font-bold text-slate-400 block mb-0.5">
                    Próximo Corte
                  </span>
                  <span className="text-sm font-extrabold text-slate-900 dark:text-white">
                    {formatFriendlyDate(bestOption.nextCutoffDate)}
                  </span>
                  <span className="text-[11px] text-slate-500 dark:text-slate-400 block">
                    En {bestOption.daysUntilCutoff} días
                  </span>
                </div>

                <div className="p-3 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 shadow-sm">
                  <span className="text-[10px] uppercase font-bold text-slate-400 block mb-0.5">
                    Fecha Límite Pago
                  </span>
                  <span className="text-sm font-extrabold text-blue-600 dark:text-blue-400">
                    {formatFriendlyDate(bestOption.paymentDueDate)}
                  </span>
                  <span className="text-[11px] text-slate-500 dark:text-slate-400 block">
                    Día {bestOption.card.paymentDueDay}
                  </span>
                </div>

                <div className="p-3 rounded-2xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 shadow-sm col-span-2 sm:col-span-1">
                  <span className="text-[10px] uppercase font-bold text-slate-400 block mb-0.5">
                    Financiamiento
                  </span>
                  <span className="text-sm font-extrabold text-emerald-600 dark:text-emerald-400">
                    {bestOption.daysOfFinancing} Días
                  </span>
                  <span className="text-[11px] text-slate-500 dark:text-slate-400 block">
                    Máximo plazo
                  </span>
                </div>
              </div>

              {/* Holiday Shift Warning (Banxico Article 23) */}
              {bestOption.isPaymentShiftedByHoliday && (
                <div className="p-2.5 rounded-xl bg-blue-50 dark:bg-blue-950/40 border border-blue-200 dark:border-blue-900/60 flex items-center gap-2 text-xs text-blue-800 dark:text-blue-300">
                  <ShieldCheck className="w-4 h-4 text-blue-600 dark:text-blue-400 shrink-0" />
                  <span>
                    <strong>Art. 23 Ley de Transparencia Financiera:</strong> Tu fecha límite de pago cayó en día inhábil bancario y se recorre al siguiente día hábil oficial.
                  </span>
                </div>
              )}

              <button
                onClick={() => onOpenAddExpense(bestOption.card.id)}
                className="w-full sm:w-auto px-5 py-2.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white dark:bg-white dark:text-slate-900 dark:hover:bg-slate-100 font-bold text-sm shadow-md transition-all flex items-center justify-center gap-2"
              >
                <span>Usar {bestOption.card.name} para mi compra hoy</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Filter Tabs */}
      <div className="flex items-center justify-between border-b border-slate-200 dark:border-slate-800 pb-3">
        <h3 className="text-base font-extrabold text-slate-900 dark:text-white">
          Todas tus Tarjetas Evaluadas Hoy
        </h3>

        <div className="flex items-center gap-1.5 p-1 bg-slate-100 dark:bg-slate-800 rounded-xl">
          <button
            onClick={() => setFilterType('ALL')}
            className={`px-3 py-1 text-xs font-bold rounded-lg transition-colors ${
              filterType === 'ALL'
                ? 'bg-white dark:bg-slate-900 text-slate-900 dark:text-white shadow-sm'
                : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
            }`}
          >
            Todas ({recommendations.length})
          </button>
          <button
            onClick={() => setFilterType('BANKING')}
            className={`px-3 py-1 text-xs font-bold rounded-lg transition-colors ${
              filterType === 'BANKING'
                ? 'bg-white dark:bg-slate-900 text-slate-900 dark:text-white shadow-sm'
                : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
            }`}
          >
            Bancarias ({recommendations.filter(r => !r.card.isDepartmental).length})
          </button>
          <button
            onClick={() => setFilterType('DEPARTMENTAL')}
            className={`px-3 py-1 text-xs font-bold rounded-lg transition-colors ${
              filterType === 'DEPARTMENTAL'
                ? 'bg-white dark:bg-slate-900 text-slate-900 dark:text-white shadow-sm'
                : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
            }`}
          >
            Departamentales ({recommendations.filter(r => r.card.isDepartmental).length})
          </button>
        </div>
      </div>

      {/* Cards List Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {filteredRecommendations.map(rec => {
          const trafficColor =
            rec.trafficLight === 'GREEN'
              ? 'text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200 dark:border-emerald-800'
              : rec.trafficLight === 'YELLOW'
              ? 'text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/40 border-amber-200 dark:border-amber-800'
              : 'text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-950/40 border-red-200 dark:border-red-800';

          return (
            <div
              key={rec.card.id}
              className="p-5 rounded-2xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700 shadow-sm transition-all flex flex-col justify-between space-y-4"
            >
              <div className="flex items-start justify-between">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <h4 className="text-base font-extrabold text-slate-900 dark:text-white">
                      {rec.card.name}
                    </h4>
                    {rec.card.isDepartmental && (
                      <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-md bg-purple-100 text-purple-800 dark:bg-purple-900/50 dark:text-purple-300">
                        Departamental
                      </span>
                    )}
                  </div>
                  <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
                    {rec.card.bank} •••• {rec.card.last4Digits}
                  </span>
                </div>

                <div className={`px-3 py-1.5 rounded-xl border text-xs font-black flex items-center gap-1.5 ${trafficColor}`}>
                  <span className="w-2 h-2 rounded-full bg-current" />
                  <span>{rec.daysOfFinancing} Días</span>
                </div>
              </div>

              {/* Financing bar meter */}
              <div>
                <div className="flex justify-between text-xs text-slate-500 mb-1">
                  <span>Plazo de financiamiento</span>
                  <span className="font-bold text-slate-700 dark:text-slate-300">
                    {rec.daysOfFinancing} / 50 días
                  </span>
                </div>
                <div className="w-full bg-slate-100 dark:bg-slate-800 h-2 rounded-full overflow-hidden">
                  <div
                    className={`h-2 rounded-full ${
                      rec.trafficLight === 'GREEN'
                        ? 'bg-emerald-500'
                        : rec.trafficLight === 'YELLOW'
                        ? 'bg-amber-500'
                        : 'bg-red-500'
                    }`}
                    style={{ width: `${Math.min(100, Math.round((rec.daysOfFinancing / 50) * 100))}%` }}
                  />
                </div>
              </div>

              {/* Cutoff and Due Dates */}
              <div className="grid grid-cols-2 gap-2 text-xs pt-2 border-t border-slate-100 dark:border-slate-800">
                <div>
                  <span className="text-slate-400 uppercase font-semibold block text-[10px]">
                    Próximo Corte
                  </span>
                  <span className="font-bold text-slate-900 dark:text-white">
                    {formatFriendlyDate(rec.nextCutoffDate)}
                  </span>
                  <span className="text-slate-400 block text-[11px]">
                    Faltan {rec.daysUntilCutoff} días
                  </span>
                </div>
                <div>
                  <span className="text-slate-400 uppercase font-semibold block text-[10px]">
                    Límite de Pago
                  </span>
                  <span className="font-bold text-blue-600 dark:text-blue-400">
                    {formatFriendlyDate(rec.paymentDueDate)}
                  </span>
                  {rec.isPaymentShiftedByHoliday && (
                    <span className="text-amber-600 dark:text-amber-400 block text-[10px] font-bold">
                      Recorrido por festivo
                    </span>
                  )}
                </div>
              </div>

              <button
                onClick={() => onOpenAddExpense(rec.card.id)}
                className="w-full py-2 px-3 rounded-xl bg-slate-50 dark:bg-slate-800/80 hover:bg-blue-50 dark:hover:bg-blue-950/40 text-slate-700 dark:text-slate-300 hover:text-blue-600 dark:hover:text-blue-400 border border-slate-200 dark:border-slate-700 font-bold text-xs transition-colors flex items-center justify-center gap-1.5"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Registrar compra con esta tarjeta</span>
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
};
