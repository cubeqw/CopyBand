package com.cubeqw.copyband;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class QuoteViewHolder extends ViewHolder<String>
{
    private final TextView quoteTextView =getView().findViewById(R.id.quote);
    TextView date= getView().findViewById(R.id.date);
    private QuoteViewHolder(View itemView)
    {
        super(itemView);
    }

    public static QuoteViewHolder make(ViewGroup parent)
    {
        LayoutInflater viewInflater = LayoutInflater.from(parent.getContext());
        View quoteListItemView = viewInflater.inflate(R.layout.listitem_quote,parent,false);
        return new QuoteViewHolder(quoteListItemView);
    }

    @Override
    protected void onSetModel(String newModel)
    {
        quoteTextView.setText(newModel);
    }
    @Override
    protected void onSetModel1(String newModel)
    {
        date.setText(newModel);
    }
}
