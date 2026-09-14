import React, { useState } from 'react';
import { AppProvider } from './context/AppContext.tsx';
import { Header } from './components/Header.tsx';
import { QuickActionFab } from './components/QuickActionFab.tsx';
import { QuickAddExpenseModal } from './components/QuickAddExpenseModal.tsx';
import { QuickAddPaymentModal } from './components/QuickAddPaymentModal.tsx';
import { MinimumPaymentModal } from './components/MinimumPaymentModal.tsx';

// Screens
import { RecommendationScreen } from './screens/RecommendationScreen.tsx';
import { MsiTrackerScreen } from './screens/MsiTrackerScreen.tsx';
import { SubscriptionsScreen } from './screens/SubscriptionsScreen.tsx';
import { FuelScreen } from './screens/FuelScreen.tsx';
import { ServicesScreen } from './screens/ServicesScreen.tsx';
import { StatementsScreen } from './screens/StatementsScreen.tsx';
import { CardsManagementScreen } from './screens/CardsManagementScreen.tsx';
import { SearchScreen } from './screens/SearchScreen.tsx';
import { AccountScreen } from './screens/AccountScreen.tsx';

import { Expense } from './types.ts';
import {
  Sparkles,
  Layers,
  Repeat,
  Flame,
  Zap,
  FileText,
  CreditCard,
  Search,
  Settings,
} from 'lucide-react';

export type ScreenTab =
  | 'recommendation'
  | 'msi'
  | 'subscriptions'
  | 'fuel'
  | 'services'
  | 'statements'
  | 'cards'
  | 'search'
  | 'account';

const NAV_ITEMS: { id: ScreenTab; label: string; icon: React.FC<{ className?: string }> }[] = [
  { id: 'recommendation', label: 'Recomendador', icon: Sparkles },
  { id: 'msi', label: 'MSI & Flujo', icon: Layers },
  { id: 'statements', label: 'Estados Cuenta', icon: FileText },
  { id: 'cards', label: 'Tarjetas', icon: CreditCard },
  { id: 'subscriptions', label: 'Suscripciones', icon: Repeat },
  { id: 'fuel', label: 'Gasolina', icon: Flame },
  { id: 'services', label: 'Servicios', icon: Zap },
];

const AppContent: React.FC = () => {
  const [currentTab, setCurrentTab] = useState<ScreenTab>('recommendation');

  // Modals
  const [isExpenseModalOpen, setIsExpenseModalOpen] = useState<boolean>(false);
  const [expenseToEdit, setExpenseToEdit] = useState<Expense | null>(null);
  const [defaultExpenseCardId, setDefaultExpenseCardId] = useState<number | undefined>(undefined);

  const [isPaymentModalOpen, setIsPaymentModalOpen] = useState<boolean>(false);
  const [defaultPaymentCardId, setDefaultPaymentCardId] = useState<number | undefined>(undefined);

  const [isMinimumModalOpen, setIsMinimumModalOpen] = useState<boolean>(false);
  const [minimumBalance, setMinimumBalance] = useState<number>(15000);
  const [minimumRate, setMinimumRate] = useState<number>(55);
  const [minimumLimit, setMinimumLimit] = useState<number>(40000);

  // Helper actions
  const handleOpenAddExpense = (cardId?: number) => {
    setExpenseToEdit(null);
    setDefaultExpenseCardId(cardId);
    setIsExpenseModalOpen(true);
  };

  const handleEditExpense = (expense: Expense) => {
    setExpenseToEdit(expense);
    setDefaultExpenseCardId(expense.cardId);
    setIsExpenseModalOpen(true);
  };

  const handleOpenAddPayment = (cardId?: number) => {
    setDefaultPaymentCardId(cardId);
    setIsPaymentModalOpen(true);
  };

  const handleOpenMinimumPayoff = (balance: number, rate: number, limit: number) => {
    setMinimumBalance(balance);
    setMinimumRate(rate);
    setMinimumLimit(limit);
    setIsMinimumModalOpen(true);
  };

  const getTabHumanLabel = (tab: ScreenTab): string => {
    switch (tab) {
      case 'recommendation':
        return 'Recomendador de Tarjetas';
      case 'msi':
        return 'Meses Sin Intereses & Flujo';
      case 'statements':
        return 'Estados de Cuenta';
      case 'cards':
        return 'Billetera de Tarjetas';
      case 'subscriptions':
        return 'Suscripciones Periódicas';
      case 'fuel':
        return 'Control de Gasolina & Rendimiento';
      case 'services':
        return 'Recibos & Servicios del Hogar';
      case 'search':
        return 'Buscador Global';
      case 'account':
        return 'Ajustes & Respaldo';
      default:
        return 'Control TDC';
    }
  };

  return (
    <div className="min-h-screen flex flex-col bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 transition-colors">
      {/* Global Header */}
      <Header
        currentTab={getTabHumanLabel(currentTab)}
        onOpenSearch={() => setCurrentTab('search')}
        onOpenAccount={() => setCurrentTab('account')}
      />

      {/* Top Desktop Navigation Ribbon */}
      <div className="bg-white/80 dark:bg-slate-900/80 border-b border-slate-200 dark:border-slate-800 backdrop-blur-sm sticky top-16 z-20 overflow-x-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 flex items-center gap-1.5 py-2">
          {NAV_ITEMS.map(item => {
            const Icon = item.icon;
            const isActive = currentTab === item.id;

            return (
              <button
                key={item.id}
                onClick={() => setCurrentTab(item.id)}
                className={`flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-bold whitespace-nowrap transition-all ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-md shadow-blue-500/20 scale-[1.02]'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800'
                }`}
              >
                <Icon className="w-4 h-4" />
                <span>{item.label}</span>
              </button>
            );
          })}

          <div className="ml-auto flex items-center gap-1 pl-2 border-l border-slate-200 dark:border-slate-800">
            <button
              onClick={() => setCurrentTab('search')}
              className={`p-2 rounded-xl text-xs font-bold transition-colors ${
                currentTab === 'search'
                  ? 'bg-blue-600 text-white'
                  : 'text-slate-500 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800'
              }`}
              title="Buscador"
            >
              <Search className="w-4 h-4" />
            </button>
            <button
              onClick={() => setCurrentTab('account')}
              className={`p-2 rounded-xl text-xs font-bold transition-colors ${
                currentTab === 'account'
                  ? 'bg-blue-600 text-white'
                  : 'text-slate-500 hover:text-slate-900 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800'
              }`}
              title="Ajustes"
            >
              <Settings className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Main Content Viewport */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 pt-6 pb-24 sm:pb-12">
        {currentTab === 'recommendation' && (
          <RecommendationScreen
            onOpenAddExpense={handleOpenAddExpense}
            onOpenAddPayment={handleOpenAddPayment}
            onNavigateToStatements={() => setCurrentTab('statements')}
          />
        )}

        {currentTab === 'msi' && (
          <MsiTrackerScreen
            onOpenAddExpense={() => handleOpenAddExpense()}
            onEditExpense={handleEditExpense}
            onOpenMinimumPayoff={handleOpenMinimumPayoff}
          />
        )}

        {currentTab === 'statements' && (
          <StatementsScreen
            onOpenAddExpense={handleOpenAddExpense}
            onOpenAddPayment={handleOpenAddPayment}
            onEditExpense={handleEditExpense}
            onOpenMinimumPayoff={handleOpenMinimumPayoff}
          />
        )}

        {currentTab === 'cards' && <CardsManagementScreen />}

        {currentTab === 'subscriptions' && <SubscriptionsScreen />}

        {currentTab === 'fuel' && <FuelScreen />}

        {currentTab === 'services' && <ServicesScreen />}

        {currentTab === 'search' && <SearchScreen onEditExpense={handleEditExpense} />}

        {currentTab === 'account' && <AccountScreen />}
      </main>

      {/* Floating Action Speed Dial */}
      <QuickActionFab
        onOpenAddExpense={() => handleOpenAddExpense()}
        onOpenAddPayment={() => handleOpenAddPayment()}
        onOpenAddSubscription={() => setCurrentTab('subscriptions')}
        onOpenAddFuel={() => setCurrentTab('fuel')}
        onOpenAddService={() => setCurrentTab('services')}
      />

      {/* Mobile Bottom Navigation Bar (Matching Android app ergonomics) */}
      <nav className="sm:hidden fixed bottom-0 left-0 right-0 z-30 bg-white/95 dark:bg-slate-900/95 backdrop-blur-md border-t border-slate-200 dark:border-slate-800 px-2 py-1.5 flex items-center justify-around">
        <button
          onClick={() => setCurrentTab('recommendation')}
          className={`flex flex-col items-center gap-0.5 p-1.5 rounded-xl transition-colors ${
            currentTab === 'recommendation'
              ? 'text-blue-600 dark:text-blue-400 font-bold'
              : 'text-slate-500 dark:text-slate-400'
          }`}
        >
          <Sparkles className="w-5 h-5" />
          <span className="text-[10px]">Inicio</span>
        </button>

        <button
          onClick={() => setCurrentTab('msi')}
          className={`flex flex-col items-center gap-0.5 p-1.5 rounded-xl transition-colors ${
            currentTab === 'msi'
              ? 'text-blue-600 dark:text-blue-400 font-bold'
              : 'text-slate-500 dark:text-slate-400'
          }`}
        >
          <Layers className="w-5 h-5" />
          <span className="text-[10px]">MSI</span>
        </button>

        <button
          onClick={() => setCurrentTab('statements')}
          className={`flex flex-col items-center gap-0.5 p-1.5 rounded-xl transition-colors ${
            currentTab === 'statements'
              ? 'text-blue-600 dark:text-blue-400 font-bold'
              : 'text-slate-500 dark:text-slate-400'
          }`}
        >
          <FileText className="w-5 h-5" />
          <span className="text-[10px]">Estados</span>
        </button>

        <button
          onClick={() => setCurrentTab('cards')}
          className={`flex flex-col items-center gap-0.5 p-1.5 rounded-xl transition-colors ${
            currentTab === 'cards'
              ? 'text-blue-600 dark:text-blue-400 font-bold'
              : 'text-slate-500 dark:text-slate-400'
          }`}
        >
          <CreditCard className="w-5 h-5" />
          <span className="text-[10px]">Tarjetas</span>
        </button>

        <button
          onClick={() => setCurrentTab('account')}
          className={`flex flex-col items-center gap-0.5 p-1.5 rounded-xl transition-colors ${
            currentTab === 'account'
              ? 'text-blue-600 dark:text-blue-400 font-bold'
              : 'text-slate-500 dark:text-slate-400'
          }`}
        >
          <Settings className="w-5 h-5" />
          <span className="text-[10px]">Ajustes</span>
        </button>
      </nav>

      {/* Global Modals */}
      <QuickAddExpenseModal
        isOpen={isExpenseModalOpen}
        onClose={() => {
          setIsExpenseModalOpen(false);
          setExpenseToEdit(null);
        }}
        expenseToEdit={expenseToEdit}
        defaultCardId={defaultExpenseCardId}
      />

      <QuickAddPaymentModal
        isOpen={isPaymentModalOpen}
        onClose={() => setIsPaymentModalOpen(false)}
        defaultCardId={defaultPaymentCardId}
      />

      <MinimumPaymentModal
        isOpen={isMinimumModalOpen}
        onClose={() => setIsMinimumModalOpen(false)}
        initialBalance={minimumBalance}
        initialRate={minimumRate}
        initialLimit={minimumLimit}
      />
    </div>
  );
};

export default function App() {
  return (
    <AppProvider>
      <AppContent />
    </AppProvider>
  );
}
