package com.example.chapa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends ArrayAdapter<PaymentTransaction> {
    public TransactionAdapter(Context context, List<PaymentTransaction> transactions) {
        super(context, 0, transactions);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.transaction_item, parent, false);
        }

        PaymentTransaction transaction = getItem(position);
        if (transaction == null) {
            return convertView; // Handle potential null transaction
        }

        TextView transactionIdView = convertView.findViewById(R.id.txtTransactionId);
        TextView amountView = convertView.findViewById(R.id.txtAmount);
        TextView paymentMethodView = convertView.findViewById(R.id.paymentMethod);
        TextView statusView = convertView.findViewById(R.id.txtStatus);
        TextView timestampView = convertView.findViewById(R.id.timestamp);

        transactionIdView.setText(transaction.getTransactionId());
        amountView.setText(String.format(Locale.getDefault(), "%d Birr", transaction.getAmount()));
        paymentMethodView.setText(transaction.getPaymentMethod());
        statusView.setText(transaction.getStatus());

        // Format timestamp
        if (transaction.getTimestamp() != null) {
            Date date = transaction.getTimestamp().toDate(); // Convert Timestamp to Date
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
            timestampView.setText(formattedDate);
        } else {
            timestampView.setText("Invalid Date");
        }

        return convertView;
    }
}