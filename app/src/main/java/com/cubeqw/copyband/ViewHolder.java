package com.cubeqw.copyband;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public abstract class ViewHolder<Model> extends RecyclerView.ViewHolder
    {
        private final View view;
        private Model model;
        private Model model1;

        protected ViewHolder(View itemView)
        {
            super(itemView);
            view = itemView;
        }

        public final View getView()
        {
            return view;
        }

        public final Model getModel()
        {
            return model;
        }

        public final void setModel(Model model)
        {
            this.model = model;
            onSetModel(model);

        }  public final void setModel1(Model model)
        {
            this.model1=model;
            onSetModel1(model);

        }

        protected abstract void onSetModel(Model newModel);
        protected abstract void onSetModel1(Model newModel);


        protected final Context getContext()
        {
            return view.getContext();
        }

    }
