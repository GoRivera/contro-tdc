import React from 'react';
import { Eye, EyeOff, Moon, Sun, Search, ShieldCheck, Bell } from 'lucide-react';
import { useApp } from '../context/AppContext.tsx';

interface HeaderProps {
  currentTab: string;
  onOpenSearch: () => void;
  onOpenAccount: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  currentTab,
  onOpenSearch,
  onOpenAccount,
}) => {
  const { isPrivateMode, togglePrivateMode, isDarkMode, toggleDarkMode } = useApp();

  return (
    <header className="sticky top-0 z-30 bg-white/90 dark:bg-slate-900/90 backdrop-blur-md border-b border-slate-200 dark:border-slate-800 transition-colors">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-600 flex items-center justify-center shadow-md shadow-blue-500/20 text-white font-black tracking-wider text-sm">
            TDC
          </div>
          <div>
            <h1 className="text-lg font-bold text-slate-900 dark:text-white leading-tight">
              Control TDC
            </h1>
            <p className="text-xs font-medium text-slate-500 dark:text-slate-400 capitalize">
              {currentTab}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-1 sm:gap-2">
          {/* Privacy mode toggle */}
          <button
            id="privacy-mode-toggle"
            type="button"
            onClick={togglePrivateMode}
            title={isPrivateMode ? 'Modo Privacidad: Activo (Clic para mostrar saldos)' : 'Modo Privacidad: Inactivo (Clic para ocultar saldos)'}
            className={`p-2.5 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all ${
              isPrivateMode
                ? 'bg-amber-100 text-amber-800 dark:bg-amber-950/60 dark:text-amber-300 ring-1 ring-amber-300 dark:ring-amber-700'
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800'
            }`}
          >
            {isPrivateMode ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
            <span className="hidden md:inline text-xs">
              {isPrivateMode ? 'Privacidad ON' : 'Ocultar'}
            </span>
          </button>

          {/* Search Button */}
          <button
            id="global-search-btn"
            type="button"
            onClick={onOpenSearch}
            title="Buscar transacciones y tarjetas"
            className="p-2.5 rounded-xl text-slate-600 hover:text-slate-900 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800 transition-colors"
          >
            <Search className="w-4 h-4" />
          </button>

          {/* Theme Toggle */}
          <button
            id="theme-toggle-btn"
            type="button"
            onClick={toggleDarkMode}
            title={isDarkMode ? 'Cambiar a modo claro' : 'Cambiar a modo obscuro'}
            className="p-2.5 rounded-xl text-slate-600 hover:text-slate-900 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800 transition-colors"
          >
            {isDarkMode ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4 text-slate-600" />}
          </button>

          {/* Cloud Sync Status */}
          <button
            id="account-profile-btn"
            type="button"
            onClick={onOpenAccount}
            title="Perfil y Ajustes de Cuenta"
            className="flex items-center gap-1.5 py-1.5 px-2.5 rounded-xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 transition-colors"
          >
            <ShieldCheck className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
            <span className="text-xs font-semibold hidden sm:inline">Local + Cloud</span>
          </button>
        </div>
      </div>
    </header>
  );
};
