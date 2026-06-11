export interface Account {
  id: number | null;
  accountHolderName: string;
  accountNumber: string;
  balance: number;
  currency: string;
}

export type AccountPayload = Omit<Account, 'id'>;
