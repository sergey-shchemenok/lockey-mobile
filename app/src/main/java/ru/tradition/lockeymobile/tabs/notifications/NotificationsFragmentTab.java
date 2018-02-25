package ru.tradition.lockeymobile.tabs.notifications;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

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

    private NotificationCursorAdapter adapter;
    private static final int CURSOR_LOADER_ID = 1;

    public NotificationsFragmentTab(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.tab_fragment_notification, container, false);

        ListView petListView = (ListView) rootView.findViewById(R.id.notification_list);
        adapter = new NotificationCursorAdapter(getActivity(), null);
        petListView.setAdapter(adapter);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = rootView.findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        //listener for items
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View viewItem, int itemPosition, long itemId) {
                //todo mage NotificationActivity
//                Intent intent = new Intent(getActivity(), NotificationActivity.class);
//                //Make an URI for intent
//                Uri currentPetUri = ContentUris.withAppendedId(NotificationContract.NotificationEntry.CONTENT_URI, itemId);
//                intent.setData(currentPetUri);
//                startActivity(intent);
            }
        });

        //kick off loader
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        return rootView;

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
