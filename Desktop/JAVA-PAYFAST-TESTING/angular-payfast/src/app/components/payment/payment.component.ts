import { Component } from '@angular/core';
import { PayfastService, PaymentRequest } from '../../services/payfast.service';

@Component({
  selector: 'app-payment',
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.css']
})
export class PaymentComponent {

  payment: PaymentRequest = {
    nameFirst: '',
    nameLast: '',
    emailAddress: '',
    cellNumber: '',
    amount: 0,
    itemName: '',
    itemDescription: ''
  };

  loading = false;
  error = '';

  constructor(private payfastService: PayfastService) {}

  onSubmit(): void {
    this.loading = true;
    this.error = '';

    this.payfastService.createPayment(this.payment).subscribe({
      next: (formData) => {
        this.payfastService.submitToPayfast(formData);
      },
      error: (err) => {
        this.loading = false;
        this.error = 'Failed to initiate payment. Please try again.';
        console.error('Payment creation failed:', err);
      }
    });
  }
}
