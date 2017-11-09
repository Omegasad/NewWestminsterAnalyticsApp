package com.keyboardape.newwestminsteranalyticsapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class ToolbarFragment extends Fragment {

    public ToolbarFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_toolbar, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);

        // Hide action icon of current activity
        if (getActivity() instanceof MapsActivity) {
            menu.findItem(R.id.action_map).setVisible(false);
        } else if (getActivity() instanceof MapLayersActivity) {
            menu.findItem(R.id.action_map_layers).setVisible(false);
        } else if (getActivity() instanceof ChartActivity) {
            menu.findItem(R.id.action_charts).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                startActivity(new Intent(getActivity(), MapsActivity.class));
                return true;
            case R.id.action_map_layers:
                startActivity(new Intent(getActivity(), MapLayersActivity.class));
                return true;
            case R.id.action_charts:
                startActivity(new Intent(getActivity(), ChartActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
