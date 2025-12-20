export interface CompanyInvoice {
  id: number;
  invoiceKey: string;
  invoiceNumber: string;
  amount: number;
  currency: string;
  status: string;
  dueDate?: string;
  paidDate?: string;
  description?: string;
  notes?: string;
  stripeInvoiceId?: string;
  stripePaymentIntentId?: string;
  billingPeriodStart?: string;
  billingPeriodEnd?: string;
  companyId: number;
  companyName?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CompanyInvoiceRequest {
  companyKey: string;
  amount: number;
  currency?: string;
  status?: string;
  dueDate?: string;
  description?: string;
  notes?: string;
  stripeInvoiceId?: string;
  stripePaymentIntentId?: string;
  billingPeriodStart?: string;
  billingPeriodEnd?: string;
}

