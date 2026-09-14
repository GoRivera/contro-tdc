import React, { useState, useMemo } from 'react';
import { useApp } from '../context/AppContext.tsx';
import {
  computeMsiSummary,
  projectCashFlowRelease,
  formatCurrency,
} from '../domain/CreditCardCalculator.ts';
import { MsiSummary, Expense } from '../types.ts';
import { MsiAmortizationModal } from '../components/MsiAmortizationModal.tsx';
import {
  Sparkles,
  CheckCircle2,
  Calendar,
  CreditCard as CardIcon,
  ChevronRight,
  TrendingUp,
  Plus,
  Minus,
  Trash2,
  Edit2,
  Clock,
  Layers,
  HelpCircle,
} from 'lucide-react';

interface MsiTrackerScreenProps {
  onOpenAddExpense: () => void;
  onEditExpense: (expense: Expense) => void;
  onOpenMinimumPayoff: (balance: number, rate: number, limit: number) => void;
}

export const MsiTrackerScreen: React.FC<MsiTrackerScreenProps> = ({
  onOpenAddExpense,
  onEditExpense,
  onOpenMinimumPayoff,
}) => {
  const {
    cards,
    expenses,
    isPrivateMode,
    advanceMsiInstallment,
    undoAdvanceMsiInstallment,
    deleteExpense,
  } = useApp();

  const [activeTab, setActiveTab] = useState<'ACTIVE' | 'COMPLETED'>('ACTIVE');
  const [selectedCardFilterId, setSelectedCardFilterId] = useState<number | null>(null);
  const [selectedMsiForModal, setSelectedMsiForModal] = useState<MsiSummary | null>(null);

  // Calculate MSI summaries for all expenses with isMsi === true
  const allMsiSummaries = useMemo(() => {
    return expenses
      .filter(e => e.isMsi)
      .map(e => {
        const card = cards.find(c => c.id === e.cardId);
        return computeMsiSummary(e, card);
      });
  }, [expenses, cards]);

  const activeMsiList = useMemo(() => {
    return allMsiSummaries.filter(m => !m.isCompleted);
  }, [allMsiSummaries]);

  const completedMsiList = useMemo(() => {
    return allMsiSummaries.filter(m => m.isCompleted);
  }, [allMsiSummaries]);

  // Cash flow release projections
  const cashFlowReleases = useMemo(() => {
    return projectCashFlowRelease(activeMsiList);
  }, [activeMsiList]);

  // Totals
  const totalMonthlyMsi = useMemo(() => {
    return activeMsiList.reduce((acc, item) => acc + item.monthlyPayment, 0);
  }, [activeMsiList]);

  const totalRemainingBalance = useMemo(() => {
    return activeMsiList.reduce((acc, item) => acc + item.remainingBalance, 0);
  }, [activeMsiList]);

  // Current tab items filtered by card
  const displayedItems = useMemo(() => {
    const list = activeTab === 'ACTIVE' ? activeMsiList : completedMsiList;
    if (selectedCardFilterId === null) return list;
    return list.filter(item => item.expense.cardId === selectedCardFilterId);
  }, [activeTab, activeMsiList, completedMsiList, selectedCardFilterId]);

  return (
    <div className="space-y-6 pb-12">
      {/* MSI Hero KPI Card */}
      <div className="bg-gradient-to-br from-indigo-900 via-slate-900 to-blue-950 text-white rounded-3xl p-6 shadow-xl border border-indigo-500/20 relative overflow-hidden">
        <div className="absolute top-0 right-0 w-80 h-80 bg-indigo-500/10 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10 grid grid-cols-1 sm:grid-cols-3 gap-5 items-center">
          <div>
            <div className="flex items-center gap-2 mb-1.5">
              <Clock className="w-4 h-4 text-indigo-400" />
              <span className="text-xs font-bold uppercase tracking-wider text-indigo-300">
                Compromiso Mensual en MSI
              </span>
            </div>
            <div className="text-3xl sm:text-4xl font-black font-mono tracking-tight text-white">
              {formatCurrency(totalMonthlyMsi, isPrivateMode)}
            </div>
            <span className="text-xs text-indigo-200 mt-1 block">
              En {activeMsiList.length} planes activos
            </span>
          </div>

          <div className="sm:border-l sm:border-white/15 sm:pl-5">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-1">
              Saldo Pendiente de Liquidar
            </span>
            <div className="text-2xl sm:text-3xl font-bold font-mono text-slate-100">
              {formatCurrency(totalRemainingBalance, isPrivateMode)}
            </div>
            <span className="text-xs text-slate-400 mt-1 block">
              Suma de futuras mensualidades diferidas
            </span>
          </div>

          <div className="flex sm:justify-end">
            <button
              onClick={onOpenAddExpense}
              className="w-full sm:w-auto px-5 py-3 rounded-2xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs shadow-lg shadow-blue-600/30 transition-all flex items-center justify-center gap-2"
            >
              <Plus className="w-4 h-4" />
              <span>Registrar Compra a MSI</span>
            </button>
          </div>
        </div>
      </div>

      {/* Cash Flow Release Projections Banner */}
      {cashFlowReleases.length > 0 && (
        <div className="p-5 rounded-3xl bg-slate-900 text-white border border-slate-800 shadow-md">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-emerald-400" />
              <h3 className="text-sm font-extrabold tracking-wide uppercase text-slate-200">
                Liberación de Flujo de Efectivo Mensual
              </h3>
            </div>
            <span className="text-xs text-slate-400 font-medium">
              Dinero disponible al terminar cada plan
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {cashFlowReleases.map((release, idx) => (
              <div
                key={idx}
                className="p-3.5 rounded-2xl bg-slate-800/80 border border-slate-700/60 flex flex-col justify-between"
              >
                <div className="flex items-center justify-between mb-1.5">
                  <span className="text-xs font-bold text-slate-300">
                    {release.monthYearLabel}
                  </span>
                  <span className="text-xs font-extrabold text-emerald-400 bg-emerald-950/60 px-2 py-0.5 rounded-lg border border-emerald-800/60">
                    +{formatCurrency(release.monthlyAmountFreed, isPrivateMode)}/mes
                  </span>
                </div>
                <div className="text-[11px] text-slate-400 line-clamp-2">
                  {release.finishingItems.join(' • ')}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Navigation Tabs (Activos vs Finalizados) & Card Filter */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-200 dark:border-slate-800 pb-3">
        {/* Tabs */}
        <div className="flex items-center gap-2">
          <button
            onClick={() => setActiveTab('ACTIVE')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'ACTIVE'
                ? 'bg-blue-600 text-white shadow-md shadow-blue-600/20'
                : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:text-slate-900'
            }`}
          >
            Activos ({activeMsiList.length})
          </button>
          <button
            onClick={() => setActiveTab('COMPLETED')}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all ${
              activeTab === 'COMPLETED'
                ? 'bg-blue-600 text-white shadow-md shadow-blue-600/20'
                : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:text-slate-900'
            }`}
          >
            Finalizados ({completedMsiList.length})
          </button>
        </div>

        {/* Card Filter Chips */}
        <div className="flex items-center gap-1.5 overflow-x-auto py-1">
          <button
            onClick={() => setSelectedCardFilterId(null)}
            className={`px-3 py-1 text-xs rounded-lg font-semibold whitespace-nowrap transition-colors ${
              selectedCardFilterId === null
                ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
                : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-200'
            }`}
          >
            Todas las Tarjetas
          </button>
          {cards.map(c => (
            <button
              key={c.id}
              onClick={() => setSelectedCardFilterId(c.id)}
              className={`px-3 py-1 text-xs rounded-lg font-semibold whitespace-nowrap transition-colors ${
                selectedCardFilterId === c.id
                  ? 'bg-slate-900 text-white dark:bg-white dark:text-slate-900'
                  : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-200'
              }`}
            >
              {c.name}
            </button>
          ))}
        </div>
      </div>

      {/* MSI Items List */}
      {displayedItems.length === 0 ? (
        <div className="p-12 text-center rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 space-y-3">
          <Layers className="w-12 h-12 text-slate-300 dark:text-slate-600 mx-auto" />
          <h4 className="text-base font-bold text-slate-700 dark:text-slate-300">
            {activeTab === 'ACTIVE'
              ? 'No tienes compras a Meses Sin Intereses activas'
              : 'No hay planes de MSI finalizados en el registro'}
          </h4>
          <p className="text-xs text-slate-400 max-w-sm mx-auto">
            Registra una compra con la casilla de MSI activa para hacer seguimiento mensual y proyectar tus flujos.
          </p>
          {activeTab === 'ACTIVE' && (
            <button
              onClick={onOpenAddExpense}
              className="mt-2 px-4 py-2 rounded-xl bg-blue-600 text-white text-xs font-bold hover:bg-blue-700"
            >
              Registrar Compra a MSI
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {displayedItems.map(item => {
            const exp = item.expense;
            const currentInst = Math.min(exp.msiCurrentInstallment, exp.msiTotalMonths);
            const totalMonths = exp.msiTotalMonths;
            const percent = Math.round((currentInst / totalMonths) * 100);

            return (
              <div
                key={exp.id}
                className="p-5 rounded-3xl bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 shadow-sm hover:border-slate-300 dark:hover:border-slate-700 transition-all flex flex-col justify-between space-y-4"
              >
                {/* Header */}
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xs font-bold uppercase tracking-wider px-2.5 py-0.5 rounded-full bg-blue-100 text-blue-800 dark:bg-blue-900/50 dark:text-blue-300">
                        {totalMonths} MSI
                      </span>
                      <span className="text-xs font-medium text-slate-500 dark:text-slate-400">
                        {item.card?.name || 'TDC'}
                      </span>
                    </div>
                    <h4 className="text-base font-extrabold text-slate-900 dark:text-white leading-tight">
                      {exp.concept}
                    </h4>
                    <span className="text-xs text-slate-400">
                      Beneficiario: {exp.beneficiary} • {exp.category}
                    </span>
                  </div>

                  <div className="text-right">
                    <span className="text-[10px] uppercase font-bold text-slate-400 block">
                      Mensualidad
                    </span>
                    <span className="text-lg font-black font-mono text-slate-900 dark:text-white">
                      {formatCurrency(item.monthlyPayment, isPrivateMode)}
                    </span>
                  </div>
                </div>

                {/* Progress bar */}
                <div>
                  <div className="flex items-center justify-between text-xs mb-1.5">
                    <span className="font-semibold text-slate-700 dark:text-slate-300">
                      Mensualidad {currentInst} de {totalMonths} ({percent}%)
                    </span>
                    <span className="text-slate-400">
                      Termina: <strong className="text-slate-700 dark:text-slate-300">{item.completionDateString}</strong>
                    </span>
                  </div>
                  <div className="w-full bg-slate-100 dark:bg-slate-800 h-2.5 rounded-full overflow-hidden">
                    <div
                      className={`h-2.5 rounded-full transition-all duration-300 ${
                        item.isCompleted ? 'bg-emerald-500' : 'bg-blue-600'
                      }`}
                      style={{ width: `${percent}%` }}
                    />
                  </div>
                </div>

                {/* Financial Summary Box */}
                <div className="grid grid-cols-2 gap-2 text-xs p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
                  <div>
                    <span className="text-[10px] uppercase font-bold text-slate-400 block">
                      Saldo Restante
                    </span>
                    <span className="font-extrabold text-slate-900 dark:text-white font-mono">
                      {formatCurrency(item.remainingBalance, isPrivateMode)}
                    </span>
                  </div>
                  <div>
                    <span className="text-[10px] uppercase font-bold text-slate-400 block">
                      Total de la Compra
                    </span>
                    <span className="font-extrabold text-slate-900 dark:text-white font-mono">
                      {formatCurrency(item.totalPurchaseAmount, isPrivateMode)}
                    </span>
                  </div>
                </div>

                {/* Action Controls */}
                <div className="pt-2 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
                  <button
                    onClick={() => setSelectedMsiForModal(item)}
                    className="text-xs font-bold text-blue-600 dark:text-blue-400 hover:underline flex items-center gap-1"
                  >
                    <span>Ver Tabla de Amortización</span>
                    <ChevronRight className="w-3.5 h-3.5" />
                  </button>

                  <div className="flex items-center gap-1.5">
                    {/* Advance / Undo Buttons */}
                    {!item.isCompleted && (
                      <>
                        <button
                          onClick={() => undoAdvanceMsiInstallment(exp.id)}
                          disabled={currentInst <= 1}
                          title="Retroceder una mensualidad (-1)"
                          className="p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-30 disabled:pointer-events-none"
                        >
                          <Minus className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={() => advanceMsiInstallment(exp.id)}
                          disabled={currentInst >= totalMonths}
                          title="Avanzar / Liquidar siguiente mensualidad (+1)"
                          className="p-1.5 rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:opacity-30 disabled:pointer-events-none"
                        >
                          <Plus className="w-3.5 h-3.5" />
                        </button>
                      </>
                    )}

                    <button
                      onClick={() => onEditExpense(exp)}
                      title="Editar compra"
                      className="p-1.5 rounded-lg text-slate-500 hover:text-slate-900 hover:bg-slate-100 dark:hover:bg-slate-800"
                    >
                      <Edit2 className="w-3.5 h-3.5" />
                    </button>

                    <button
                      onClick={() => {
                        if (confirm(`¿Deseas eliminar el plan MSI "${exp.concept}"?`)) {
                          deleteExpense(exp.id);
                        }
                      }}
                      title="Eliminar compra"
                      className="p-1.5 rounded-lg text-red-500 hover:text-red-700 hover:bg-red-50 dark:hover:bg-red-950/40"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Amortization Dialog Modal */}
      {selectedMsiForModal && (
        <MsiAmortizationModal
          msiSummary={selectedMsiForModal}
          onClose={() => setSelectedMsiForModal(null)}
          onOpenMinimumPayoff={onOpenMinimumPayoff}
        />
      )}
    </div>
  );
};
