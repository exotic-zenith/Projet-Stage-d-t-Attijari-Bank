import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AccountApiService } from './account-api.service';
import { Account, AccountPayload } from './account.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly accountApiService = inject(AccountApiService);

  readonly accounts = signal<Account[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly editingId = signal<number | null>(null);

  readonly accountForm = this.formBuilder.group({
    accountHolderName: ['', [Validators.required]],
    accountNumber: ['', [Validators.required]],
    balance: [0, [Validators.required, Validators.min(0)]],
    currency: ['USD', [Validators.required]],
  });

  ngOnInit(): void {
    this.loadAccounts();
  }

  startCreate(): void {
    this.editingId.set(null);
    this.accountForm.reset({
      accountHolderName: '',
      accountNumber: '',
      balance: 0,
      currency: 'USD',
    });
  }

  startEdit(account: Account): void {
    this.editingId.set(account.id ?? null);
    this.accountForm.setValue({
      accountHolderName: account.accountHolderName,
      accountNumber: account.accountNumber,
      balance: account.balance,
      currency: account.currency,
    });
  }

  cancelEdit(): void {
    this.startCreate();
  }

  saveAccount(): void {
    if (this.accountForm.invalid) {
      return;
    }

    const rawValue = this.accountForm.getRawValue();
    const payload: AccountPayload = {
      accountHolderName: rawValue.accountHolderName?.trim() ?? '',
      accountNumber: rawValue.accountNumber?.trim() ?? '',
      balance: Number(rawValue.balance ?? 0),
      currency: rawValue.currency?.trim().toUpperCase() ?? '',
    };

    this.error.set('');

    const request =
      this.editingId() === null
        ? this.accountApiService.create(payload)
        : this.accountApiService.update(this.editingId()!, payload);

    request.subscribe({
      next: () => {
        this.startCreate();
        this.loadAccounts();
      },
      error: () => this.error.set('Could not save the account.'),
    });
  }

  deleteAccount(account: Account): void {
    if (account.id == null) {
      return;
    }

    this.error.set('');
    this.accountApiService.delete(account.id).subscribe({
      next: () => this.loadAccounts(),
      error: () => this.error.set('Could not delete the account.'),
    });
  }

  private loadAccounts(): void {
    this.loading.set(true);
    this.error.set('');
    this.accountApiService.getAll().subscribe({
      next: (accounts) => {
        this.accounts.set(accounts);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Could not load accounts.');
        this.loading.set(false);
      },
    });
  }
}
