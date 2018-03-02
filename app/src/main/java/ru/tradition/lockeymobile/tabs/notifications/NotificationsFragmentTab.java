package ru.tradition.lockeymobile.tabs.notifications;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Set;

import ru.tradition.lockeymobile.AppData;
import ru.tradition.lockeymobile.NotificationActivity;
import ru.tradition.lockeymobile.R;
import ru.tradition.lockeymobile.tabs.notifications.database.NotificationContract;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NotificationsFragmentTab.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class NotificationsFragmentTab extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOG_TAG = NotificationsFragmentTab.class.getName();

    private NotificationCursorAdapter adapter;
    private static int loaderSwitch = 0;
    private static final int CURSOR_LOADER_ID = 3;
    private static final int CURSOR_UPDATE_LOADER_ID = 4;


    //to access methods from other tabs
    public static NotificationsFragmentTab nft;

    public NotificationsFragmentTab() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.tab_fragment_notification, container, false);

        ListView notificationListView = (ListView) rootView.findViewById(R.id.notification_list);

        adapter = new NotificationCursorAdapter(getActivity(), null);
        notificationListView.setAdapter(adapter);

        //selecting mode has the other title
        if (AppData.isNotificationSelectingMode) {
            Log.i(LOG_TAG, "AppData.isNotificationSelectingMode:......." + AppData.isNotificationSelectingMode);
            AppData.mainActivity.setTitle("Выбрано: " + String.valueOf(AppData.selectedNotificationCounter));
            AppData.mainActivity.setUpButton();
            AppData.mMenu.getItem(1).setVisible(false);
        }

        //listener for items
        notificationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View viewItem, int position, long itemId) {
                //todo magic for NotificationActivity
                //if it is normal mode go to asset activity
                if (!AppData.isNotificationSelectingMode) {
                    Intent intent = new Intent(getActivity(), NotificationActivity.class);
                    //Make an URI for intent
                    Uri currentNotificationUri = ContentUris.withAppendedId(NotificationContract.NotificationEntry.CONTENT_URI, itemId);
                    intent.setData(currentNotificationUri);
                    startActivity(intent);
                }
                //if it is selecting mode. Select or deselect asset
                else {
                    Uri currentNotificationUri = ContentUris.withAppendedId(NotificationContract.NotificationEntry.CONTENT_URI, itemId);
                    if (!AppData.selectedNotification.contains(currentNotificationUri.toString())) {
                        AppData.selectedNotification.add(currentNotificationUri.toString());
                        AppData.selectedNotificationUri.add(currentNotificationUri);
                        AppData.mainActivity.setTitle("Выбрано: " + String.valueOf(++AppData.selectedNotificationCounter));
                        updateList();
                        Log.i(LOG_TAG, "currentNotificationUri.........." + currentNotificationUri.toString());
                    } else {
                        AppData.selectedNotification.remove(currentNotificationUri.toString());
                        AppData.selectedNotificationUri.remove(currentNotificationUri);
                        AppData.mainActivity.setTitle("Выбрано: " + String.valueOf(--AppData.selectedNotificationCounter));
                        updateList();
                    }
                }
            }
        });

        notificationListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long itemId) {
                Uri currentNotificationUri = ContentUris.withAppendedId(NotificationContract.NotificationEntry.CONTENT_URI, itemId);
                if (!AppData.selectedNotificationLong.contains(currentNotificationUri.toString())) {
                    AppData.selectedNotificationLong.add(currentNotificationUri.toString());
                    updateList();
                } else {
                    AppData.selectedNotificationLong.remove(currentNotificationUri.toString());
                    updateList();
                }
                return true;
            }
        });
        //kick off loader

        nft = this;
        getList();

        return rootView;

    }


    /*
    Methods for deleting pet from database
     */
    public void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.delete_notifications);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the notifications.
                deleteNotifications();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //to delete all notifications
    private void deleteAllNotifications() {
        int rowsDeleted = getActivity().getContentResolver().delete(NotificationContract.NotificationEntry.CONTENT_URI, null, null);
        Toast.makeText(getContext(), getString(R.string.notifications_deleted),
                Toast.LENGTH_SHORT).show();
        Log.v(LOG_TAG, rowsDeleted + " rows deleted from notifications database");
    }

    //to delete selected notification
    private void deleteNotifications() {
        if (AppData.selectedNotificationUri != null && !AppData.selectedNotificationUri.isEmpty()) {
            int toDelete = AppData.selectedNotificationCounter;
            Log.i(LOG_TAG, "toDelete initial........." + toDelete);
            for (Uri u : AppData.selectedNotificationUri) {
                if (u != null) {
                    int rowsAffected = getActivity().getContentResolver().delete(u, null, null);
                    toDelete -= rowsAffected;
                    Log.i(LOG_TAG, "toDelete initial........." + toDelete);

                    if (AppData.selectedNotificationLong.contains(u.toString()))
                        AppData.selectedNotificationLong.remove(u.toString());

                }
            }
            // Show a toast message depending on whether or not the delete was successful.

            if (toDelete != 0) {
                // If no rows were affected, then there was an error with the delete.
                Toast.makeText(getContext(), getString(R.string.editor_delete_notification_failed) + " " + AppData.selectedNotificationCounter,
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(getContext(), getString(R.string.editor_delete_notification_successful) + " " + AppData.selectedNotificationCounter,
                        Toast.LENGTH_SHORT).show();
            }
            AppData.mainActivity.changeModeToNormal();
        }
    }

    public void getList() {
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }

    //update listView after changes
    public void updateList() {
        if (loaderSwitch == 0) {
            getLoaderManager().initLoader(CURSOR_UPDATE_LOADER_ID, null, this);
            getLoaderManager().getLoader(CURSOR_LOADER_ID).reset();
            loaderSwitch = 1;
        } else if (loaderSwitch == 1) {
            getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
            getLoaderManager().getLoader(CURSOR_UPDATE_LOADER_ID).reset();
            loaderSwitch = 2;
        } else if (loaderSwitch == 2) {
            getLoaderManager().restartLoader(CURSOR_UPDATE_LOADER_ID, null, this);
            getLoaderManager().getLoader(CURSOR_LOADER_ID).reset();
            loaderSwitch = 1;
        }
    }

    @Override
    public void onDestroyView() {
        getLoaderManager().destroyLoader(CURSOR_LOADER_ID);
        getLoaderManager().destroyLoader(CURSOR_UPDATE_LOADER_ID);
        loaderSwitch = 0;
        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                //id column is always needed for the cursor
                NotificationContract.NotificationEntry._ID,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_TITLE,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_BODY,
                NotificationContract.NotificationEntry.COLUMN_NOTIFICATION_SENDING_TIME,


        };

        String sortOrder =
                NotificationContract.NotificationEntry._ID + " DESC";
        return new CursorLoader(getContext(), NotificationContract.NotificationEntry.CONTENT_URI, projection,
                null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    //    @Override
//    public void onDestroyView() {
//        MainActivity.isFinished = false;
//        MainActivity.isRepeated = false;
//        super.onDestroyView();
//    }


    private OnFragmentInteractionListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
