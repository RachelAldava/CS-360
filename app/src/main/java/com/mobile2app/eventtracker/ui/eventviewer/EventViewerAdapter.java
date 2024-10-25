package com.mobile2app.eventtracker.ui.eventviewer;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mobile2app.eventtracker.DatabaseHandler;
import com.mobile2app.eventtracker.R;
import com.mobile2app.eventtracker.User;
import com.mobile2app.eventtracker.appEvent;
import com.mobile2app.eventtracker.databinding.FragmentEventTableRowBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventViewerAdapter extends RecyclerView.Adapter<EventViewerAdapter.ViewHolder> {

    public final List<appEvent> retrievedEvents;

    public EventViewerAdapter(Context context) {
        this.retrievedEvents = DatabaseHandler.getInstance(context).readEvents();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new ViewHolder(FragmentEventTableRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));

    }

    // Allows for new card insertions, which can be edited in order to achieve CREATE functionality
    public void addEntry(appEvent event){
        try {retrievedEvents.add(event);}
        catch (Exception e){return;} // The instance is not created
        super.notifyItemInserted(retrievedEvents.size() - 1);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.populateElementValues(retrievedEvents.get(position));

        // Interfaces with the database to DELETE an event.
        holder.cardDelete.setOnClickListener(view -> {
            // Find and pluck the event from the list
            int recyclerIndex = holder.getAdapterPosition();
            retrievedEvents.remove(recyclerIndex);

            // Disarm the alarm
            holder.cardEvent.cancelAlarm(view.getContext());

            // Connect to DB and remove row corresponding to the event's ID number.
            int eventID = holder.cardEvent.getId();
            DatabaseHandler db = DatabaseHandler.getInstance(view.getContext());
            db.deleteEvent(eventID);

            // Notify the adapter of the change
            EventViewerAdapter.super.notifyItemRemoved(recyclerIndex);
        });

        holder.cardEdit.setOnClickListener(holder::toggleEdit);
        holder.cardEditDate.setOnClickListener(holder::processEditDate);
        holder.cardEditTime.setOnClickListener(holder::proccessEditTime);
        holder.cardEditReminderDate.setOnClickListener(holder::processEditDateReminder);
        holder.cardEditReminderTime.setOnClickListener(holder::proccessEditTimeReminder);
        holder.cardSave.setOnClickListener(holder::SaveInput);
    }

    @Override
    public int getItemCount() {
        return retrievedEvents.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView cardTitle, cardDescription, cardDate, cardTime, cardTimeHeader, cardDateReminder, cardTimeReminder, cardReminderHeader;
        public final Button cardDelete, cardEdit, cardSave, cardEditDate, cardEditTime, cardEditReminderTime, cardEditReminderDate;
        public final EditText editTitle, editDescription;
        public appEvent cardEvent;
        private boolean editMode;

        public localEvent unsavedEvent;

        public class localEvent{
            public String title;
            public String description;

            public List<String> owners;
            public List<String> participants;

            public localdate startTime;
            public localdate reminderTime;

            localEvent(){
                this.startTime=new localdate();
                this.reminderTime=new localdate();
            }


        }

        public class localdate{
            public int year;
            public int month;
            public int day;
            public int hour;
            public int minute;
            private boolean hasDate = false;
            private boolean hasTime = false;

            localdate(){}
            public void setDate(int day, int month, int year){
                this.day = day;
                this.month = month;
                this.year = year;
                hasDate = true;
            }

            public void setTime(int hour, int minute){
                this.hour = hour;
                this.minute = minute;
                hasTime = true;
            }

            public Date toDate(){
                if (!hasDate) return null;
                if (!hasTime) return new Date(year,month,day);
                return new Date(year,month,day, hour, minute);
            }


        }

        public ViewHolder(FragmentEventTableRowBinding binding) {
            super(binding.getRoot());
            cardTitle = binding.cardEventTitle;
            cardDescription= binding.cardEventDescription;
            cardDate = binding.textDate;
            cardTime = binding.textTime;
            cardTimeHeader = binding.textTimeHeader;
            cardDateReminder = binding.textDateReminder;
            cardTimeReminder = binding.textTimeReminder;
            cardReminderHeader = binding.textReminderHeader;

            cardDelete = binding.eventRowDelete;
            cardEdit = binding.eventRowEdit;
            cardSave = binding.eventRowSave;
            cardEditDate = binding.editDate;
            cardEditTime = binding.editTime;
            cardEditReminderDate = binding.editReminderDate;
            cardEditReminderTime = binding.editReminderTime;

            editTitle = binding.cardEventTitleEdit;
            editDescription = binding.cardEventDescriptionEdit;

            editMode = false;
            cardEvent = null;
            unsavedEvent = new localEvent();

        }

        public void toggleEdit(View view){
            editMode = !editMode;
            int nonEditComponentVisibility;
            int editComponentVisibility;
            int timeTextColor;
            String editButtonText;
            String reminderHeader;

            if (editMode){
                nonEditComponentVisibility = View.GONE;
                editComponentVisibility = View.VISIBLE;
                //timeTextHeight *=2;
                timeTextColor = ContextCompat.getColor(view.getContext(), R.color.md_theme_error);
                editButtonText = "cancel";
                reminderHeader = "ADD REMINDER:";
                this.unsavedEvent = new localEvent();
            }
            else{
                editComponentVisibility = View.GONE;
                nonEditComponentVisibility = View.VISIBLE;
                timeTextColor = ContextCompat.getColor(view.getContext(), R.color.md_theme_onBackground);
                editButtonText = "edit";
                reminderHeader = "Reminder:";
            }

            // Main text display toggle
            cardTitle.setVisibility(nonEditComponentVisibility);
            cardDescription.setVisibility(nonEditComponentVisibility);
            editTitle.setVisibility(editComponentVisibility);
            editDescription.setVisibility(editComponentVisibility);

            // Transform time elements
            cardTimeHeader.setVisibility(editComponentVisibility);

            cardDate.setTextColor(timeTextColor);
            cardTime.setTextColor(timeTextColor);
            cardDateReminder.setTextColor(timeTextColor);
            cardTimeReminder.setTextColor(timeTextColor);
            cardReminderHeader.setText(reminderHeader);

            // Toggle buttons
            cardDelete.setVisibility(nonEditComponentVisibility);
            cardSave.setVisibility(editComponentVisibility);
            cardEditDate.setVisibility(editComponentVisibility);
            cardEditTime.setVisibility(editComponentVisibility);
            cardEditReminderDate.setVisibility(editComponentVisibility);
            cardEditReminderTime.setVisibility(editComponentVisibility);

            // toggle off textboxes that I can't seem to update whenever someone enters a time.
            cardDate.setVisibility(nonEditComponentVisibility);
            cardTime.setVisibility(nonEditComponentVisibility);
            cardDateReminder.setVisibility(nonEditComponentVisibility);
            cardTimeReminder.setVisibility(nonEditComponentVisibility);


            // Change the edit button's text
            cardEdit.setText(editButtonText);

        }

        public void processEditDate(View view){
            // Get Current Time
            Calendar c = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(),
                    (view12, year, monthOfYear, dayOfMonth) -> unsavedEvent.startTime.setDate(dayOfMonth, monthOfYear, year), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
            datePickerDialog.show();
        }

        public void proccessEditTime(View view){
            // Get Current Time
            Calendar c = Calendar.getInstance();

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(),
                    (view1, hourOfDay, minute) -> unsavedEvent.startTime.setTime(hourOfDay, minute), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
            timePickerDialog.show();


        }

        public void processEditDateReminder(View view){
            // Get Current Time
            Calendar c = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(),
                    (view12, year, monthOfYear, dayOfMonth) -> unsavedEvent.reminderTime.setDate(dayOfMonth, monthOfYear, year), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
            datePickerDialog.show();
        }

        public void proccessEditTimeReminder(View view){
            // Get Current Time
            Calendar c = Calendar.getInstance();

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(view.getContext(),
                    (view1, hourOfDay, minute) -> unsavedEvent.reminderTime.setTime(hourOfDay, minute), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
            timePickerDialog.show();
        }

        public void SaveInput(View view){
            unsavedEvent.title = editTitle.getText().toString();
            unsavedEvent.description = editDescription.getText().toString();

            boolean writingNewEvent = (cardEvent == null);

            Integer id;
            String title = unsavedEvent.title;
            String description = unsavedEvent.description;
            List<String> owners = new ArrayList<>();// TODO:: Allow users to update and change owners and participants
            owners.add(User.getCurrUsername());
            List<String> participants = null;
            Long startTime;
            Long reminderTime;

            if (writingNewEvent) id = null;
            else id = cardEvent.getId();

            try {
                if (writingNewEvent || unsavedEvent.startTime.hasDate) {
                    if (unsavedEvent.startTime.hasDate) {
                        startTime = (new Date(
                                unsavedEvent.startTime.year,
                                unsavedEvent.startTime.month,
                                unsavedEvent.startTime.day,
                                unsavedEvent.startTime.hour,
                                unsavedEvent.startTime.minute
                        ).getTime());
                    } else {
                        startTime = null;
                    }
                } else if (unsavedEvent.startTime.hasTime && cardEvent != null) {
                    Date previous = new Date(cardEvent.startTime);
                    startTime = (new Date(
                            previous.getYear(),
                            previous.getMonth(),
                            previous.getDay(),
                            unsavedEvent.startTime.hour,
                            unsavedEvent.startTime.minute
                    ).getTime());
                } else if (cardEvent == null) {
                    startTime = null;
                } else {
                    startTime = cardEvent.startTime;
                }

                if (startTime == null) {
                    reminderTime = null;
                } else if (writingNewEvent || unsavedEvent.reminderTime.hasDate) {
                    if (unsavedEvent.reminderTime.hasDate) {
                        reminderTime = (new Date(
                                unsavedEvent.reminderTime.year,
                                unsavedEvent.reminderTime.month,
                                unsavedEvent.reminderTime.day,
                                unsavedEvent.reminderTime.hour,
                                unsavedEvent.reminderTime.minute
                        ).getTime());
                    } else {
                        reminderTime = null;
                    }
                } else if (unsavedEvent.reminderTime.hasTime && cardEvent != null) {
                    Date previous = new Date(cardEvent.reminderTime);
                    reminderTime = (new Date(
                            previous.getYear(),
                            previous.getMonth(),
                            previous.getDay(),
                            unsavedEvent.reminderTime.hour,
                            unsavedEvent.reminderTime.minute
                    ).getTime());
                } else if (cardEvent == null) {
                    reminderTime = null;
                } else {
                    reminderTime = cardEvent.reminderTime;
                }
            } catch (Exception E) {// An unknown issue occurred
                startTime = null;
                reminderTime = null;
            }

            DatabaseHandler db = DatabaseHandler.getInstance(view.getContext());
            appEvent updatedApp = db.writeEvent(id, title, description, owners,  participants, startTime, reminderTime);
            unsavedEvent = new localEvent();

            try{
            cardEvent.cancelAlarm(view.getContext());}
            catch (Exception ignored){}
            populateElementValues(updatedApp);
            toggleEdit(view);
        }


        String noDate = "    No Date";
        String noTime = "    No Time";
        String newTitle = "New Event Title";
        String newDescription = "New Event Description";

        public void populateElementValues(appEvent event){
            cardEvent = event;
            if (event == null) {

                cardTitle.setText(newTitle);
                cardDescription.setText(newDescription);
                cardDate.setText(noDate);
                cardTime.setText(noTime);
                cardDateReminder.setText(noDate);
                cardTimeReminder.setText(noTime);
                return;
            }
            cardTitle.setText(event.getTitle());
            cardDescription.setText(event.getDescription());

            Long start = event.startTime;
            Long remind = event.reminderTime;

            if (start == null) {
                cardDate.setText(noDate);
                cardTime.setText("");
            }
            else{
                cardDate.setText(appEvent.dateFormatShort(start));
                cardTime.setText(appEvent.timeFormatShort(start));
            }

            if (remind == null) {
                cardDateReminder.setText(noDate);
                cardTimeReminder.setText("");
            }
            else{
                cardDateReminder.setText(appEvent.dateFormatShort(remind));
                cardTimeReminder.setText(appEvent.timeFormatShort(remind));
            }

        }
    }
}