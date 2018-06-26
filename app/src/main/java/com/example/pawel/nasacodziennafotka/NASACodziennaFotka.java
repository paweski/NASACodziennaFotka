package com.example.pawel.nasacodziennafotka;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;



class NASACodziennaFotka extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nasacodzienna_fotka);

        IotdHandler handler = new IotdHandler();
        MojeZadanieWTle parsuj = new MojeZadanieWTle(handler);

        parsuj.execute();

    }

    public void resetDisplay(String tytulFotki, String dataFotki, Bitmap fotka, String opisFotki)
    {

        TextView titleView = (TextView) findViewById(R.id.textView);
        titleView.setText(tytulFotki);

        TextView dateView = (TextView) findViewById(R.id.textView3);
        dateView.setText(dataFotki);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(fotka);

        TextView descriptionView = (TextView) findViewById(R.id.textView4);
        descriptionView.setText(opisFotki);
    }


    public class MojeZadanieWTle extends AsyncTask<Void, Void, Void>
    {
        private IotdHandler iotdHandler;

        public MojeZadanieWTle(IotdHandler iotdHandler)
        {
            this.iotdHandler = iotdHandler;

        }

        @Override
        protected Void doInBackground(Void...params)
        {
            iotdHandler.processFeed();
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            resetDisplay( iotdHandler.getTytulFotki(),iotdHandler.getDateFotki(), iotdHandler.getObrazekFotki(), iotdHandler.getOpisFotki() );
        }

    }

}
