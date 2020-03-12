package com.example.sched;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.media.Image;
import android.provider.CalendarContract;
import android.telephony.CellIdentity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class calendar extends LinearLayout {

    ImageButton prev, next;
    TextView Current_date;
    GridView gridview;

    private static final  int MAX= 42;
    Calendar calendar=Calendar.getInstance(Locale.ENGLISH);
    Context context;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);
    SimpleDateFormat monthFormat= new SimpleDateFormat("MMM", Locale.ENGLISH);
    SimpleDateFormat yearFormat = new SimpleDateFormat("YYYY",Locale.ENGLISH);

    MyGridAdapter myGridAdapter;
    AlertDialog alertDialog;
    List<Date> dates=new ArrayList<java.util.Date>();
    List<Events> eventsList=new ArrayList<>();
    private DBOpenHelper dbOpenHelper;

    public calendar(Context context) {
        super(context);
    }

    public calendar(final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        InitializeLayout();
        SetUpCalendar();

        prev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.MONTH, -1);
                SetUpCalendar();
            }
        });

        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.add(Calendar.MONTH,1);
                SetUpCalendar();
            }
        });

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder builder=new AlertDialog.Builder(context);
                builder.setCancelable(true);
                final View addview=LayoutInflater.from(parent.getContext()).inflate(R.layout.add_event, null);
                final EditText EventName =  addview.findViewById(R.id.event_id);
                final TextView EventTime= addview.findViewById(R.id.event_time);
                ImageButton SetTime=addview.findViewById(R.id.set_event_time);
                Button AddEvent=addview.findViewById(R.id.add_event);
                SetTime.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar calendar=Calendar.getInstance();
                        int hours=calendar.get(Calendar.HOUR_OF_DAY);
                        int minutes= calendar.get(Calendar.MINUTE);
                        TimePickerDialog timePickerDialog = new TimePickerDialog(addview.getContext(), R.style.Theme_AppCompat_Dialog, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar c=Calendar.getInstance();
                                c.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                c.set(Calendar.MINUTE,minute);
                                c.setTimeZone(TimeZone.getDefault());
                                SimpleDateFormat hformat=new SimpleDateFormat("K:mm a",Locale.ENGLISH);

                                String event_Time = hformat.format(c.getTime());
                                EventTime.setText(event_Time);
                            }
                        },hours,minutes,false);
                        timePickerDialog.show();
                    }
                });

               final String date = dateFormat.format(dates.get(position));
               final String month = monthFormat.format(dates.get(position));
               final String year = yearFormat.format(dates.get(position));

                AddEvent.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SaveEvent(EventName.getText().toString(),EventTime.getText().toString(),date,month,year);
                        SetUpCalendar();
                        alertDialog.dismiss();

                    }

                });

                builder.setView(addview);
                alertDialog=builder.create();
                alertDialog.show();
            }
        });





    }

    public calendar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    private void SaveEvent(String event, String time, String date, String month, String year)
    {
        dbOpenHelper=new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.SaveEvent(event,time,date,month, year,database);
        dbOpenHelper.close();
        Toast.makeText(context, "Event saved", Toast.LENGTH_SHORT).show();
    }

    private void InitializeLayout()
    {
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.calendar_layout, this);
        next=view.findViewById(R.id.next);
        prev=view.findViewById(R.id.previous);
        Current_date=view.findViewById(R.id.current);
        gridview=view.findViewById(R.id.grid);
    }

    private void SetUpCalendar()
    {
        String currentDate= dateFormat.format((calendar.getTime()));
        Current_date.setText(currentDate);
        dates.clear();
        Calendar monthCalendar=(Calendar) calendar.clone();
        monthCalendar.set(Calendar.DAY_OF_MONTH,1);
        int FirstDayOfMonth= monthCalendar.get(Calendar.DAY_OF_WEEK)-1;
        monthCalendar.add(Calendar.DAY_OF_MONTH, -FirstDayOfMonth);

        while (dates.size() < MAX)
        {
            dates.add(monthCalendar.getTime());
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }


        myGridAdapter=new MyGridAdapter(context,dates,calendar,eventsList);
        gridview.setAdapter(myGridAdapter);
    }

    class Events {
    }
}
