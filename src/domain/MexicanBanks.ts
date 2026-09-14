export interface MexicanBankInfo {
  name: string;
  primaryColorHex: string;
  secondaryColorHex: string;
  defaultNetwork: 'Visa' | 'Mastercard' | 'Amex' | 'Departamental';
  isDepartmental: boolean;
}

export const MEXICAN_BANKS: MexicanBankInfo[] = [
  { name: "BBVA", primaryColorHex: "#004481", secondaryColorHex: "#1464A5", defaultNetwork: "Visa", isDepartmental: false },
  { name: "Santander", primaryColorHex: "#EC0000", secondaryColorHex: "#990000", defaultNetwork: "Mastercard", isDepartmental: false },
  { name: "Banamex", primaryColorHex: "#002D72", secondaryColorHex: "#001F3F", defaultNetwork: "Mastercard", isDepartmental: false },
  { name: "Banorte", primaryColorHex: "#EB0029", secondaryColorHex: "#1B1D22", defaultNetwork: "Mastercard", isDepartmental: false },
  { name: "Nu México", primaryColorHex: "#820AD1", secondaryColorHex: "#4C0677", defaultNetwork: "Mastercard", isDepartmental: false },
  { name: "HSBC", primaryColorHex: "#DB0011", secondaryColorHex: "#222222", defaultNetwork: "Visa", isDepartmental: false },
  { name: "Scotiabank", primaryColorHex: "#ED0722", secondaryColorHex: "#880000", defaultNetwork: "Visa", isDepartmental: false },
  { name: "American Express", primaryColorHex: "#0077A6", secondaryColorHex: "#002F6C", defaultNetwork: "Amex", isDepartmental: false },
  { name: "Plata Card", primaryColorHex: "#2A2E35", secondaryColorHex: "#5C677D", defaultNetwork: "Mastercard", isDepartmental: false },
  { name: "Inbursa", primaryColorHex: "#003865", secondaryColorHex: "#C8102E", defaultNetwork: "Visa", isDepartmental: false },
  { name: "Banco Azteca", primaryColorHex: "#007A33", secondaryColorHex: "#004D20", defaultNetwork: "Mastercard", isDepartmental: false },
  { name: "BanCoppel", primaryColorHex: "#005691", secondaryColorHex: "#FFD100", defaultNetwork: "Visa", isDepartmental: false },
  { name: "Hey Banco", primaryColorHex: "#1A1A1A", secondaryColorHex: "#FFB800", defaultNetwork: "Visa", isDepartmental: false },
  { name: "Banregio", primaryColorHex: "#FF5A00", secondaryColorHex: "#CC4400", defaultNetwork: "Visa", isDepartmental: false },
  { name: "Afirme", primaryColorHex: "#00833E", secondaryColorHex: "#005A2B", defaultNetwork: "Mastercard", isDepartmental: false },
  { name: "Liverpool", primaryColorHex: "#E10098", secondaryColorHex: "#88005C", defaultNetwork: "Visa", isDepartmental: true },
  { name: "Palacio de Hierro", primaryColorHex: "#B8860B", secondaryColorHex: "#7A5806", defaultNetwork: "Departamental", isDepartmental: true },
  { name: "Sears", primaryColorHex: "#0A2540", secondaryColorHex: "#B21E27", defaultNetwork: "Departamental", isDepartmental: true },
  { name: "Mercado Pago", primaryColorHex: "#009EE3", secondaryColorHex: "#006699", defaultNetwork: "Visa", isDepartmental: false },
  { name: "RappiCard", primaryColorHex: "#FF441F", secondaryColorHex: "#1F1F1F", defaultNetwork: "Visa", isDepartmental: false },
  { name: "Klar", primaryColorHex: "#00D1B2", secondaryColorHex: "#002244", defaultNetwork: "Mastercard", isDepartmental: false },
  { name: "Stori", primaryColorHex: "#00C853", secondaryColorHex: "#003322", defaultNetwork: "Mastercard", isDepartmental: false },
  { name: "INVEX", primaryColorHex: "#002D62", secondaryColorHex: "#8A0027", defaultNetwork: "Mastercard", isDepartmental: false },
  { name: "Costco", primaryColorHex: "#005DAA", secondaryColorHex: "#E31837", defaultNetwork: "Visa", isDepartmental: true },
  { name: "Suburbia", primaryColorHex: "#6A1B9A", secondaryColorHex: "#4A148C", defaultNetwork: "Departamental", isDepartmental: true }
];

export function findMatchingBanks(query: string): MexicanBankInfo[] {
  const trimmed = query.trim().toLowerCase();
  if (!trimmed) return MEXICAN_BANKS;
  return MEXICAN_BANKS.filter(b => b.name.toLowerCase().includes(trimmed));
}

export function findExactOrBestBank(query: string): MexicanBankInfo | undefined {
  const trimmed = query.trim().toLowerCase();
  return (
    MEXICAN_BANKS.find(b => b.name.toLowerCase() === trimmed) ||
    MEXICAN_BANKS.find(b => b.name.toLowerCase().includes(trimmed))
  );
}
