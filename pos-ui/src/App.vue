<script setup>
import CashierDashboard from './views/CashierDashboard.vue';
</script>

<template>
  <CashierDashboard />
</template>


<style>
/* --- Core Variables & CSS Reset --- */
:root {
  --bg-gradient: linear-gradient(135deg, #111827 0%, #1f2937 100%);
  --panel-bg: rgba(31, 41, 55, 0.45);
  --card-bg: rgba(55, 65, 81, 0.4);
  --border-color: rgba(255, 255, 255, 0.08);
  --text-primary: #f9fafb;
  --text-secondary: #9ca3af;
  --accent-color: #6366f1;
  --accent-glow: rgba(99, 102, 241, 0.3);
  --success-color: #10b981;
  --success-glow: rgba(16, 185, 129, 0.3);
  --danger-color: #ef4444;
  --danger-glow: rgba(239, 68, 68, 0.3);
  --warning-color: #f59e0b;
}

body {
  margin: 0;
  padding: 0;
  background: #0f172a;
  color: var(--text-primary);
  font-family: 'Outfit', 'Inter', system-ui, sans-serif;
  overflow-x: hidden;
}

#app {
  width: 100% !important;
  max-width: 100% !important;
  min-height: 100vh;
  border-inline: none !important;
}

.pos-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  padding: 20px;
  box-sizing: border-box;
  gap: 20px;
}

/* --- Header --- */
.pos-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.2);
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-area h1 {
  font-size: 24px;
  margin: 0;
  font-weight: 700;
  letter-spacing: -0.5px;
  color: var(--text-primary);
}

.logo-area .badge {
  font-size: 11px;
  padding: 2px 6px;
  background: var(--accent-color);
  border-radius: 6px;
  font-weight: 600;
  vertical-align: middle;
}

.transaction-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.order-id {
  font-size: 14px;
  color: var(--text-secondary);
}

.order-id strong {
  color: var(--text-primary);
}

.status-pill {
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 20px;
  margin-left: 8px;
  font-weight: 600;
  text-transform: uppercase;
}

.status-pill.draft {
  background: rgba(99, 102, 241, 0.2);
  color: #a5b4fc;
  border: 1px solid rgba(99, 102, 241, 0.4);
}

.status-pill.checkout_pending {
  background: rgba(245, 158, 11, 0.2);
  color: #fde047;
  border: 1px solid rgba(245, 158, 11, 0.4);
}

.status-pill.paid {
  background: rgba(16, 185, 129, 0.2);
  color: #6ee7b7;
  border: 1px solid rgba(16, 185, 129, 0.4);
}

/* --- Global Alert Banner --- */
.alert-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  animation: slideDown 0.3s ease;
}

.alert-banner.info { background: rgba(59, 130, 246, 0.15); border: 1px solid rgba(59, 130, 246, 0.3); color: #93c5fd; }
.alert-banner.success { background: rgba(16, 185, 129, 0.15); border: 1px solid rgba(16, 185, 129, 0.3); color: #6ee7b7; }
.alert-banner.warning { background: rgba(245, 158, 11, 0.15); border: 1px solid rgba(245, 158, 11, 0.3); color: #fde047; }
.alert-banner.error { background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.3); color: #fca5a5; }

/* --- Workspace Grid --- */
.pos-workspace {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 20px;
  flex-grow: 1;
}

@media (max-width: 1024px) {
  .pos-workspace {
    grid-template-columns: 1fr;
  }
}

.workspace-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 24px;
  background: var(--panel-bg);
  backdrop-filter: blur(16px);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25);
  transition: opacity 0.3s ease;
  position: relative;
}

.disabled-overlay {
  opacity: 0.45;
  pointer-events: none;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 12px;
}

.panel-header h2 {
  font-size: 20px;
  margin: 0;
  font-weight: 600;
}

.step-num {
  font-size: 12px;
  padding: 2px 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: var(--text-secondary);
}

/* --- Cards (Glassmorphic) --- */
.glass-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.glass-card h3 {
  font-size: 15px;
  margin: 0 0 12px;
  font-weight: 600;
  color: var(--text-secondary);
}

.form-row {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.relative {
  position: relative;
}

.flex-1 { flex: 1; }
.flex-2 { flex: 2; }

label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
}

input, select {
  background: rgba(17, 24, 39, 0.6);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 10px 14px;
  color: var(--text-primary);
  font-family: inherit;
  font-size: 14px;
  transition: all 0.3s ease;
}

input:focus, select:focus {
  border-color: var(--accent-color);
  outline: none;
  box-shadow: 0 0 0 3px var(--accent-glow);
}

/* --- Autocomplete Dropdown --- */
.autocomplete-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  background: #1e293b;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  max-height: 200px;
  overflow-y: auto;
  z-index: 100;
  list-style: none;
  padding: 0;
  margin: 4px 0 0;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.5);
}

.autocomplete-dropdown li {
  padding: 10px 14px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255, 255, 255, 0.03);
}

.autocomplete-dropdown li:hover {
  background: rgba(255, 255, 255, 0.08);
}

.suggestion-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  gap: 8px;
}

.suggestion-item .s-name {
  color: var(--text-primary);
  flex-grow: 1;
  text-align: left;
}

.suggestion-item .s-id {
  color: var(--text-secondary);
  font-size: 11px;
}

.suggestion-item .s-price {
  color: var(--warning-color);
  font-weight: bold;
}

/* --- Scanner Styling --- */
.scanner-row {
  align-items: flex-end;
}

.btn-scan {
  height: 42px;
  align-self: flex-end;
}

/* --- Items Table --- */
.items-table-container {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.items-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: 14px;
}

.items-table th, .items-table td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-color);
}

.items-table th {
  background: rgba(17, 24, 39, 0.4);
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
}

.items-table tr:hover td {
  background: rgba(255, 255, 255, 0.02);
}

.item-name {
  font-weight: 600;
}

.item-id-sub {
  font-size: 11px;
  color: var(--text-secondary);
}

.discount-text {
  color: var(--warning-color);
  font-weight: 600;
}

.font-bold { font-weight: 700; }
.text-right { text-align: right; }
.text-center { text-align: center; }

.empty-placeholder {
  text-align: center;
  color: var(--text-secondary);
  padding: 40px !important;
}

/* --- Promotions Block --- */
.promotions-summary {
  background: rgba(245, 158, 11, 0.06);
  border: 1px dashed rgba(245, 158, 11, 0.3);
  border-radius: 8px;
  padding: 12px;
}

.promotions-summary h4 {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--warning-color);
  font-weight: 600;
}

.promo-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.promo-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.promo-badge {
  font-size: 10px;
  padding: 2px 6px;
  background: rgba(245, 158, 11, 0.2);
  color: var(--warning-color);
  border-radius: 4px;
  font-weight: 700;
}

.promo-discount {
  color: var(--warning-color);
  font-weight: 700;
}

/* --- Totals Summary block --- */
.totals-block {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 16px;
  background: rgba(17, 24, 39, 0.4);
  border-radius: 12px;
  margin-top: auto;
}

.total-row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}

.discount-row {
  color: var(--warning-color);
}

.final-row {
  border-top: 1px solid var(--border-color);
  padding-top: 8px;
  font-size: 18px;
  font-weight: 800;
  color: var(--text-primary);
}

/* --- Button Styling --- */
button {
  cursor: pointer;
  font-family: inherit;
  font-size: 14px;
  font-weight: 600;
  border-radius: 8px;
  padding: 10px 18px;
  border: none;
  transition: all 0.3s ease;
}

.btn-primary {
  background: var(--accent-color);
  color: white;
  box-shadow: 0 4px 14px var(--accent-glow);
}

.btn-primary:hover {
  background: #4f46e5;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px var(--accent-glow);
}

.btn-secondary {
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.btn-secondary:hover {
  background: rgba(255, 255, 255, 0.15);
}

.btn-success {
  background: var(--success-color);
  color: white;
  box-shadow: 0 4px 14px var(--success-glow);
}

.btn-success:hover {
  background: #059669;
  transform: translateY(-1px);
  box-shadow: 0 6px 20px var(--success-glow);
}

.btn-danger {
  background: var(--danger-color);
  color: white;
  box-shadow: 0 4px 14px var(--danger-glow);
}

.btn-danger:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

.btn-lg {
  padding: 12px 24px;
  font-size: 16px;
}

.btn-block {
  width: 100%;
}

.btn-icon-delete {
  padding: 4px 8px;
  background: rgba(239, 68, 68, 0.15);
  color: var(--danger-color);
  border-radius: 4px;
  font-size: 12px;
}

.btn-icon-delete:hover {
  background: var(--danger-color);
  color: white;
}

.btn-pulse {
  animation: pulse 2s infinite;
}

.btn-glow {
  box-shadow: 0 0 15px var(--success-color);
}

/* --- Panel Actions Layout --- */
.panel-actions {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-top: 10px;
}

.column-actions {
  flex-direction: column;
}

/* --- Payment Panel Elements --- */
.payment-status-card {
  background: linear-gradient(135deg, rgba(30, 41, 59, 0.8) 0%, rgba(15, 23, 42, 0.9) 100%);
  border: 1px solid var(--border-color);
}

.status-gauge {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.gauge-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.gauge-item .label {
  font-size: 11px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.gauge-item .value {
  font-size: 20px;
}

.flex-column-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.status-badge-paid {
  font-size: 14px;
  padding: 6px 12px;
  background: var(--success-color);
  color: white;
  border-radius: 20px;
  font-weight: 700;
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

/* --- Payment Ledger --- */
.payments-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.payments-table th, .payments-table td {
  padding: 10px 12px;
  border-bottom: 1px solid var(--border-color);
}

.payments-table th {
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 11px;
  text-align: left;
}

.pay-method-badge {
  font-size: 10px;
  font-weight: 700;
  padding: 3px 6px;
  border-radius: 4px;
}

.pay-method-badge.cash {
  background: rgba(16, 185, 129, 0.15);
  color: #34d399;
  border: 1px solid rgba(16, 185, 129, 0.3);
}

.pay-method-badge.credit_card {
  background: rgba(99, 102, 241, 0.15);
  color: #a5b4fc;
  border: 1px solid rgba(99, 102, 241, 0.3);
}

/* --- Payment Tab & Options --- */
.payment-tabs {
  display: flex;
  gap: 8px;
  background: rgba(17, 24, 39, 0.4);
  padding: 4px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.tab-btn {
  flex: 1;
  background: transparent;
  color: var(--text-secondary);
  padding: 8px;
  font-size: 13px;
  border-radius: 6px;
}

.tab-btn.active {
  background: var(--card-bg);
  color: var(--text-primary);
  box-shadow: var(--shadow);
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  animation: fadeIn 0.3s ease;
}

.input-with-currency {
  position: relative;
  display: flex;
  align-items: center;
}

.currency-symbol {
  position: absolute;
  left: 14px;
  font-weight: bold;
  color: var(--text-secondary);
}

.input-with-currency input {
  padding-left: 30px;
  width: 100%;
  box-sizing: border-box;
}

.calculator-helpers {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.helper-btn {
  flex: 1;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  font-size: 11px;
  padding: 6px 10px;
  border-radius: 6px;
}

.helper-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.change-preview-box {
  background: rgba(245, 158, 11, 0.04);
  border: 1px solid rgba(245, 158, 11, 0.2);
  border-radius: 8px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
}

.preview-row {
  display: flex;
  justify-content: space-between;
}

.change-row {
  font-size: 14px;
  color: var(--warning-color);
  border-top: 1px dashed rgba(245, 158, 11, 0.3);
  padding-top: 4px;
}

.btn-submit-payment {
  width: 100%;
  padding: 12px;
}

.card-simulator-box {
  background: rgba(99, 102, 241, 0.05);
  border: 1px dashed rgba(99, 102, 241, 0.3);
  border-radius: 8px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.simulator-notice {
  font-size: 12px;
  color: #a5b4fc;
  margin: 0;
}

.card-accent {
  background: linear-gradient(135deg, #4f46e5 0%, #6366f1 100%);
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4);
}

.card-accent:hover {
  background: linear-gradient(135deg, #4338ca 0%, #4f46e5 100%);
}

/* --- Empty state Welcome --- */
.empty-state-welcome {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-grow: 1;
  padding: 40px;
}

.welcome-box {
  max-width: 500px;
  text-align: center;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.welcome-icon {
  font-size: 48px;
  animation: spinSlow 8s linear infinite;
}

/* --- Audit Logs Section --- */
.audit-logs-section {
  background: rgba(17, 24, 39, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 16px 20px;
}

.audit-header h3 {
  font-size: 14px;
  margin: 0 0 4px;
  font-weight: 600;
  color: var(--text-primary);
}

.audit-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.log-cards-container {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 10px 0;
}

.log-placeholder {
  font-size: 12px;
  color: var(--text-secondary);
  padding: 10px;
}

.log-card {
  flex: 0 0 220px;
  background: rgba(31, 41, 55, 0.6);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 10px;
  font-size: 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.log-card.cancelled {
  border-left: 4px solid var(--danger-color);
}

.log-card.paid {
  border-left: 4px solid var(--success-color);
}

.log-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-row.sub-info {
  color: var(--text-secondary);
}

.log-badge {
  font-size: 9px;
  padding: 2px 4px;
  border-radius: 4px;
  font-weight: 700;
}

.log-badge.cancelled {
  background: rgba(239, 68, 68, 0.2);
  color: var(--danger-color);
}

.log-badge.paid {
  background: rgba(16, 185, 129, 0.2);
  color: var(--success-color);
}

/* --- Animations --- */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.03); }
  100% { transform: scale(1); }
}

@keyframes pulseGlow {
  0% { text-shadow: 0 0 4px var(--danger-color); }
  50% { text-shadow: 0 0 12px var(--danger-color); }
  100% { text-shadow: 0 0 4px var(--danger-color); }
}

.pulse-glow {
  animation: pulseGlow 1.5s infinite;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(4px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes spinSlow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
