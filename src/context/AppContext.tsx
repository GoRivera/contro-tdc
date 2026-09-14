import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  CreditCard,
  Expense,
  Payment,
  Subscription,
  FuelEntry,
  ServiceEntry,
  UserProfile,
} from '../types.ts';
import {
  INITIAL_CARDS,
  INITIAL_EXPENSES,
  INITIAL_PAYMENTS,
  INITIAL_SUBSCRIPTIONS,
  INITIAL_FUEL_ENTRIES,
  INITIAL_SERVICES,
} from '../data/initialData.ts';

interface AppContextType {
  cards: CreditCard[];
  expenses: Expense[];
  payments: Payment[];
  subscriptions: Subscription[];
  fuelEntries: FuelEntry[];
  services: ServiceEntry[];
  serviceEntries: ServiceEntry[];
  isPrivateMode: boolean;
  isDarkMode: boolean;
  userProfile: UserProfile;
  setIsPrivateMode: React.Dispatch<React.SetStateAction<boolean>>;
  setIsDarkMode: React.Dispatch<React.SetStateAction<boolean>>;
  togglePrivateMode: () => void;
  toggleDarkMode: () => void;
  updateUserProfile: (profile: Partial<UserProfile>) => void;
  addCard: (card: Omit<CreditCard, 'id'>) => CreditCard;
  updateCard: (card: CreditCard) => void;
  deleteCard: (id: number) => void;
  toggleCardActive: (id: number) => void;
  addExpense: (expense: Omit<Expense, 'id'>) => Expense;
  updateExpense: (expense: Expense) => void;
  deleteExpense: (id: number) => void;
  advanceMsiInstallment: (expenseId: number) => void;
  undoAdvanceMsiInstallment: (expenseId: number) => void;
  addPayment: (payment: Omit<Payment, 'id'>) => Payment;
  deletePayment: (id: number) => void;
  addSubscription: (sub: Omit<Subscription, 'id'>) => Subscription;
  updateSubscription: (sub: Subscription) => void;
  deleteSubscription: (id: number) => void;
  addFuelEntry: (entry: Omit<FuelEntry, 'id'>) => FuelEntry;
  deleteFuelEntry: (id: number) => void;
  addServiceEntry: (entry: Omit<ServiceEntry, 'id'>) => ServiceEntry;
  deleteServiceEntry: (id: number) => void;
  resetToDefaultData: () => void;
  resetToSampleData: () => void;
  exportDataToJson: () => string;
  exportDataJson: () => string;
  importDataFromJson: (jsonStr: string) => boolean;
  importDataJson: (jsonStr: string) => boolean;
}

const AppContext = createContext<AppContextType | null>(null);

function loadFromStorage<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(`control_tdc_${key}`);
    if (raw) return JSON.parse(raw);
  } catch (err) {
    console.error(`Error reading ${key} from storage:`, err);
  }
  return fallback;
}

function saveToStorage<T>(key: string, value: T): void {
  try {
    localStorage.setItem(`control_tdc_${key}`, JSON.stringify(value));
  } catch (err) {
    console.error(`Error saving ${key} to storage:`, err);
  }
}

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [cards, setCards] = useState<CreditCard[]>(() => loadFromStorage('cards', INITIAL_CARDS));
  const [expenses, setExpenses] = useState<Expense[]>(() => loadFromStorage('expenses', INITIAL_EXPENSES));
  const [payments, setPayments] = useState<Payment[]>(() => loadFromStorage('payments', INITIAL_PAYMENTS));
  const [subscriptions, setSubscriptions] = useState<Subscription[]>(() => loadFromStorage('subscriptions', INITIAL_SUBSCRIPTIONS));
  const [fuelEntries, setFuelEntries] = useState<FuelEntry[]>(() => loadFromStorage('fuelEntries', INITIAL_FUEL_ENTRIES));
  const [services, setServices] = useState<ServiceEntry[]>(() => loadFromStorage('services', INITIAL_SERVICES));

  const [isPrivateMode, setIsPrivateMode] = useState<boolean>(() => loadFromStorage('isPrivateMode', false));
  const [isDarkMode, setIsDarkMode] = useState<boolean>(() => loadFromStorage('isDarkMode', false));
  const [userProfile, setUserProfile] = useState<UserProfile>(() =>
    loadFromStorage('userProfile', {
      name: 'Gerardo Rivera',
      email: 'gorivera@gmail.com',
      currency: 'MXN',
    })
  );

  // Sync to storage
  useEffect(() => saveToStorage('cards', cards), [cards]);
  useEffect(() => saveToStorage('expenses', expenses), [expenses]);
  useEffect(() => saveToStorage('payments', payments), [payments]);
  useEffect(() => saveToStorage('subscriptions', subscriptions), [subscriptions]);
  useEffect(() => saveToStorage('fuelEntries', fuelEntries), [fuelEntries]);
  useEffect(() => saveToStorage('services', services), [services]);
  useEffect(() => saveToStorage('isPrivateMode', isPrivateMode), [isPrivateMode]);
  useEffect(() => saveToStorage('isDarkMode', isDarkMode), [isDarkMode]);
  useEffect(() => saveToStorage('userProfile', userProfile), [userProfile]);

  useEffect(() => {
    if (isDarkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [isDarkMode]);

  const togglePrivateMode = () => setIsPrivateMode(prev => !prev);
  const toggleDarkMode = () => setIsDarkMode(prev => !prev);

  const updateUserProfile = (patch: Partial<UserProfile>) => {
    setUserProfile(prev => ({ ...prev, ...patch }));
  };

  const addCard = (cardData: Omit<CreditCard, 'id'>): CreditCard => {
    const newId = Date.now();
    const newCard: CreditCard = { ...cardData, id: newId };
    setCards(prev => [...prev, newCard]);
    return newCard;
  };

  const updateCard = (card: CreditCard) => {
    setCards(prev => prev.map(c => (c.id === card.id ? card : c)));
  };

  const deleteCard = (id: number) => {
    setCards(prev => prev.filter(c => c.id !== id));
  };

  const toggleCardActive = (id: number) => {
    setCards(prev => prev.map(c => (c.id === id ? { ...c, isActive: !c.isActive } : c)));
  };

  const addExpense = (expData: Omit<Expense, 'id'>): Expense => {
    const newId = Date.now();
    const newExpense: Expense = { ...expData, id: newId };
    setExpenses(prev => [newExpense, ...prev]);
    return newExpense;
  };

  const updateExpense = (expense: Expense) => {
    setExpenses(prev => prev.map(e => (e.id === expense.id ? expense : e)));
  };

  const deleteExpense = (id: number) => {
    setExpenses(prev => prev.filter(e => e.id !== id));
  };

  const advanceMsiInstallment = (expenseId: number) => {
    setExpenses(prev =>
      prev.map(e => {
        if (e.id === expenseId && e.isMsi) {
          const nextInstallment = e.msiCurrentInstallment + 1;
          return {
            ...e,
            msiCurrentInstallment: nextInstallment,
          };
        }
        return e;
      })
    );
  };

  const undoAdvanceMsiInstallment = (expenseId: number) => {
    setExpenses(prev =>
      prev.map(e => {
        if (e.id === expenseId && e.isMsi && e.msiCurrentInstallment > 1) {
          return {
            ...e,
            msiCurrentInstallment: e.msiCurrentInstallment - 1,
          };
        }
        return e;
      })
    );
  };

  const addPayment = (paymentData: Omit<Payment, 'id'>): Payment => {
    const newId = Date.now();
    const newPayment: Payment = { ...paymentData, id: newId };
    setPayments(prev => [newPayment, ...prev]);
    return newPayment;
  };

  const deletePayment = (id: number) => {
    setPayments(prev => prev.filter(p => p.id !== id));
  };

  const addSubscription = (subData: Omit<Subscription, 'id'>): Subscription => {
    const newId = Date.now();
    const newSub: Subscription = { ...subData, id: newId };
    setSubscriptions(prev => [newSub, ...prev]);
    return newSub;
  };

  const updateSubscription = (sub: Subscription) => {
    setSubscriptions(prev => prev.map(s => (s.id === sub.id ? sub : s)));
  };

  const deleteSubscription = (id: number) => {
    setSubscriptions(prev => prev.filter(s => s.id !== id));
  };

  const addFuelEntry = (fuelData: Omit<FuelEntry, 'id'>): FuelEntry => {
    const newId = Date.now();
    const newFuel: FuelEntry = { ...fuelData, id: newId };
    setFuelEntries(prev => [newFuel, ...prev]);
    return newFuel;
  };

  const deleteFuelEntry = (id: number) => {
    setFuelEntries(prev => prev.filter(f => f.id !== id));
  };

  const addServiceEntry = (serviceData: Omit<ServiceEntry, 'id'>): ServiceEntry => {
    const newId = Date.now();
    const newService: ServiceEntry = { ...serviceData, id: newId };
    setServices(prev => [newService, ...prev]);
    return newService;
  };

  const deleteServiceEntry = (id: number) => {
    setServices(prev => prev.filter(s => s.id !== id));
  };

  const resetToDefaultData = () => {
    setCards(INITIAL_CARDS);
    setExpenses(INITIAL_EXPENSES);
    setPayments(INITIAL_PAYMENTS);
    setSubscriptions(INITIAL_SUBSCRIPTIONS);
    setFuelEntries(INITIAL_FUEL_ENTRIES);
    setServices(INITIAL_SERVICES);
  };

  const exportDataToJson = (): string => {
    const backup = {
      version: 1,
      exportedAt: new Date().toISOString(),
      userProfile,
      cards,
      expenses,
      payments,
      subscriptions,
      fuelEntries,
      services,
    };
    return JSON.stringify(backup, null, 2);
  };

  const importDataFromJson = (jsonStr: string): boolean => {
    try {
      const parsed = JSON.parse(jsonStr);
      if (parsed.cards && Array.isArray(parsed.cards)) setCards(parsed.cards);
      if (parsed.expenses && Array.isArray(parsed.expenses)) setExpenses(parsed.expenses);
      if (parsed.payments && Array.isArray(parsed.payments)) setPayments(parsed.payments);
      if (parsed.subscriptions && Array.isArray(parsed.subscriptions)) setSubscriptions(parsed.subscriptions);
      if (parsed.fuelEntries && Array.isArray(parsed.fuelEntries)) setFuelEntries(parsed.fuelEntries);
      if (parsed.services && Array.isArray(parsed.services)) setServices(parsed.services);
      if (parsed.userProfile) setUserProfile(parsed.userProfile);
      return true;
    } catch (err) {
      console.error('Import error:', err);
      return false;
    }
  };

  return (
    <AppContext.Provider
      value={{
        cards,
        expenses,
        payments,
        subscriptions,
        fuelEntries,
        services,
        serviceEntries: services,
        isPrivateMode,
        isDarkMode,
        userProfile,
        setIsPrivateMode,
        setIsDarkMode,
        togglePrivateMode,
        toggleDarkMode,
        updateUserProfile,
        addCard,
        updateCard,
        deleteCard,
        toggleCardActive,
        addExpense,
        updateExpense,
        deleteExpense,
        advanceMsiInstallment,
        undoAdvanceMsiInstallment,
        addPayment,
        deletePayment,
        addSubscription,
        updateSubscription,
        deleteSubscription,
        addFuelEntry,
        deleteFuelEntry,
        addServiceEntry,
        deleteServiceEntry,
        resetToDefaultData,
        resetToSampleData: resetToDefaultData,
        exportDataToJson,
        exportDataJson: exportDataToJson,
        importDataFromJson,
        importDataJson: importDataFromJson,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = (): AppContextType => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within an AppProvider');
  }
  return context;
};
