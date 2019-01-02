package com.example.pawel.nasacodziennafotka;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.TextView;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * Created by pawel on 19.12.16.
 * NASA fotka dnia - SAX parser konfiguracja i wyciągnięcie ze strony NASA (rss)
 * obrazka, opisu oraz daty i tytułu
 */

public class IotdHandler extends DefaultHandler {
    private static final String TAG = IotdHandler.class.getSimpleName();

    private boolean inTitle = false;
    private boolean inDescription = false;
    private int descriptionNumber = 0; // iteruje po opisach wielu zdjęć umieszczonych na stronie
    private int bitmapNumber = 0; // iteruje po bitmapach zdjęć umieszczonych na stronie
    private boolean inItem = false;
    private boolean inDate = false;
    private boolean inUrl = false;

    private String bitmap_url = null;
    private String title = null;
    private StringBuffer description = new StringBuffer();
    private String date = null;
    private Bitmap image = null;


    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) {
        if (localName.startsWith("item"))
            {
                inItem = true;
            }
        else if (inItem)
                {
                    inTitle = localName.equals("title");

                    if ( localName.startsWith("description") )
                        {
                            inDescription = true;
                            descriptionNumber++;
                        }
                    else
                        {
                            inDescription = false;
                        }

                    if (localName.startsWith("enclosure"))
                        {
                            inUrl = true;
                            bitmapNumber++;
                            bitmap_url = attributes.getValue("url");
                        // zmieniam url na https bo strona www robi http 301 http->https
                            bitmap_url = bitmap_url.substring(0,4) +'s' + bitmap_url.substring(4);
                        }

                    else
                        {
                            inUrl = false;
                        }

                    inDate = localName.equals("pubDate");
                }
    }


    public void characters(char ch[], int start, int length) {
        String chars = (new String(ch).substring(start, start + length));

        if ( inUrl && bitmapNumber == 1  ) // weź pierwsze - aktualne zdjęcie z kanału rss
            {
                image = getBitmap(bitmap_url);
                inUrl = false;
            }

        if (inTitle && title == null)
            {
                title = chars;
            }


        if ( inDescription && descriptionNumber == 1 ) // weź pierwszy aktualny opis do zdjęcia
            {
                description.append(chars);
            }


        if (inDate && date == null)
            {
                //Example: Tue, 21 Dec 2010 00:00 EST
                try
                    {
                        SimpleDateFormat parseFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.US);
                        Date sourceDate = parseFormat.parse(chars);

                        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.US);
                        date = outputFormat.format(sourceDate);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
            }

    }

    void processFeed( ) {
        try {


            URL url;


            url = new URL("https://www.nasa.gov/rss/dyn/lg_image_of_the_day.rss");

            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(this);
            xr.parse(new InputSource(url.openStream()));

        } catch (IOException | SAXException | ParserConfigurationException e) {
            Log.e(TAG, e.toString());
        }
    }

    private Bitmap getBitmap(String bitmap_url)
    {
        try
            {
            HttpURLConnection connection =
                    (HttpURLConnection) new URL(bitmap_url).openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream is = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();
            return bitmap;
            } catch (IOException ioe)
            {
            return null;
            }
    }

    String getTytulFotki() {return title; }
    String getOpisFotki() {
        return description.toString();
    }
    String getDateFotki() {
        return date;
    }
    Bitmap getObrazekFotki()
    {
        return image;
    }
}
