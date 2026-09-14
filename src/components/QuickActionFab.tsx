import React, { useState } from 'react';
import { Plus, CreditCard, ArrowDownLeft, Flame, Zap, Repeat } from 'lucide-react';

interface QuickActionFabProps {
  onOpenAddExpense: () => void;
  onOpenAddPayment: () => void;
  onOpenAddSubscription: () => void;
  onOpenAddFuel: () => void;
  onOpenAddService: () => void;
}

export const QuickActionFab: React.FC<QuickActionFabProps> = ({
  onOpenAddExpense,
  onOpenAddPayment,
  onOpenAddSubscription,
  onOpenAddFuel,
  onOpenAddService,
}) => {
  const [isOpen, setIsOpen] = useState<boolean>(false);

  return (
    <div className="fixed bottom-20 right-4 sm:bottom-6 sm:right-6 z-40 flex flex-col items-end">
      {/* Speed Dial Menu Items */}
      {isOpen && (
        <div className="flex flex-col items-end gap-2.5 mb-3 animate-fade-in">
          <button
            onClick={() => {
              setIsOpen(false);
              onOpenAddExpense();
            }}
            className="flex items-center gap-2 px-3.5 py-2 rounded-2xl bg-blue-600 text-white font-bold text-xs shadow-lg shadow-blue-500/30 hover:bg-blue-700 transition-all hover:scale-105"
          >
            <span>Registrar Gasto</span>
            <CreditCard className="w-4 h-4" />
          </button>

          <button
            onClick={() => {
              setIsOpen(false);
              onOpenAddPayment();
            }}
            className="flex items-center gap-2 px-3.5 py-2 rounded-2xl bg-emerald-600 text-white font-bold text-xs shadow-lg shadow-emerald-500/30 hover:bg-emerald-700 transition-all hover:scale-105"
          >
            <span>Registrar Abono / Pago</span>
            <ArrowDownLeft className="w-4 h-4" />
          </button>

          <button
            onClick={() => {
              setIsOpen(false);
              onOpenAddSubscription();
            }}
            className="flex items-center gap-2 px-3.5 py-2 rounded-2xl bg-purple-600 text-white font-bold text-xs shadow-lg shadow-purple-500/30 hover:bg-purple-700 transition-all hover:scale-105"
          >
            <span>Nueva Suscripción</span>
            <Repeat className="w-4 h-4" />
          </button>

          <button
            onClick={() => {
              setIsOpen(false);
              onOpenAddFuel();
            }}
            className="flex items-center gap-2 px-3.5 py-2 rounded-2xl bg-amber-600 text-white font-bold text-xs shadow-lg shadow-amber-500/30 hover:bg-amber-700 transition-all hover:scale-105"
          >
            <span>Carga de Gasolina</span>
            <Flame className="w-4 h-4" />
          </button>

          <button
            onClick={() => {
              setIsOpen(false);
              onOpenAddService();
            }}
            className="flex items-center gap-2 px-3.5 py-2 rounded-2xl bg-cyan-600 text-white font-bold text-xs shadow-lg shadow-cyan-500/30 hover:bg-cyan-700 transition-all hover:scale-105"
          >
            <span>Pago de Servicio</span>
            <Zap className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Main Floating Trigger Button */}
      <button
        id="main-action-fab"
        onClick={() => setIsOpen(prev => !prev)}
        className={`w-14 h-14 rounded-2xl shadow-xl flex items-center justify-center transition-all duration-300 ${
          isOpen
            ? 'bg-slate-800 text-white rotate-45 shadow-slate-900/40'
            : 'bg-blue-600 hover:bg-blue-700 text-white shadow-blue-600/40 hover:scale-105'
        }`}
      >
        <Plus className="w-6 h-6 stroke-[2.5]" />
      </button>
    </div>
  );
};
