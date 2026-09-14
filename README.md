# Control TDC - Gestión Financiera de Tarjetas de Crédito

Aplicación web moderna y completa para el control financiero, optimización de compras y seguimiento de tarjetas de crédito en México, portada desde la aplicación Android original a React + TypeScript + Tailwind CSS.

## Características Principales Portadas

- **Recomendador Inteligente de Compras (Semáforo Financiero)**:
  - Cálculo de días de financiamiento libre de intereses (hasta 50+ días) según la fecha de compra y fechas de corte/límite de pago.
  - Reglas del sistema bancario mexicano: ajuste de fecha límite de pago si cae en fin de semana o día feriado oficial (Ley para la Transparencia y Ordenamiento de los Servicios Financieros).
  - Regla de tarjetas departamentales: nunca se seleccionan como opción prioritaria general.
  - Indicadores semafóricos (Verde: ≥38 días, Amarillo: 23–37 días, Rojo: ≤22 días).

- **Gestión Integral de Tarjetas de Crédito**:
  - Catálogo de bancos mexicanos con paleta visual y marcas (BBVA, Banorte, Santander, Citibanamex, Nu, Hey Banco, Liverpool, Mercado Pago, Palacio de Hierro, etc.).
  - Línea de crédito total, saldo ocupado, crédito disponible y porcentaje de ocupación en tiempo real.
  - Presets automáticos de tarjetas populares en México con tasas y anualidades preconfiguradas.

- **Rastreador de Meses Sin Intereses (MSI)**:
  - Vista detallada de cada compra diferida, mensualidad actual, meses restantes y fecha estimada de liquidación.
  - Avance y retroceso de mensualidad con un solo clic.
  - Proyección de liberación de flujo de efectivo mes a mes.
  - Modal interactivo de tabla de amortización por cuotas.

- **Estados de Cuenta y Analytics**:
  - Desglose consolidado por mes y por tarjeta individual.
  - Cálculo de "Pago para no generar intereses", compras corrientes, mensualidades MSI y abonos.
  - Gráficos interactivos de distribución por categoría y por beneficiario (para compras compartidas).
  - Simulador de pago mínimo con cálculo de intereses compuestos según tasa CAT y desglose de IVA (16%).

- **Control de Suscripciones y Pagos Recurrentes**:
  - Días restantes para el próximo cobro con barra de proximidad.
  - Total proyectado de gastos recurrentes por mes.

- **Módulos Complementarios de Gastos**:
  - **Combustible**: Bitácora de cargas de gasolina (Verde/Roja), cálculo de rendimiento (km/L) y división de gastos con acompañantes.
  - **Servicios del Hogar**: Registro de luz (CFE kWh), agua (m³), gas y telecomunicaciones.
  - **Buscador Global**: Búsqueda instantánea con filtros por tarjeta, categoría, beneficiario y rango de fechas.

- **Privacidad y Datos**:
  - Modo Privacidad para ofuscar montos en pantallas públicas.
  - Modo Oscuro / Modo Claro de alto contraste.
  - Exportación e importación de respaldo en formato JSON.
  - Persistencia local en el navegador (`localStorage`).

## Ejecución Local

```bash
# Instalar dependencias
npm install

# Iniciar servidor de desarrollo
npm run dev

# Compilar para producción
npm run build
```
