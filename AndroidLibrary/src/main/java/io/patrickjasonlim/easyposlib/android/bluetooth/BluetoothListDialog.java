package io.patrickjasonlim.easyposlib.android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Set;

import io.patrickjasonlim.easyposlib.android.Constants;
import io.patrickjasonlim.easyposlib.base.PrinterInfo;

import static android.app.Activity.RESULT_OK;

public class BluetoothListDialog extends DialogFragment implements BTDeviceManager.OnBluetoothDeviceDiscoveryListener, BTDevicesListAdapter.EventListener {

    public static final String TAG = BluetoothListDialog.class.getSimpleName();
    private static final long DISCOVERY_DURATION_MS = 20000;

    private RecyclerView rvBtDevicesList;
    private TextView tvSelectedPrinter;
    private TextView tvMacAddress;
    private Button btnRemove;

    private BTDevicesListAdapter mDevicesListAdapter;
    private BTDeviceManager mBtDeviceManager;
    private EventHandler mEventHandler;

    private PrinterInfo currentSelectedPrinter;

    public static BluetoothListDialog newInstance(EventHandler mEventHandler) {

        Bundle args = new Bundle();

        BluetoothListDialog fragment = new BluetoothListDialog();
        fragment.setArguments(args);
        fragment.mEventHandler = mEventHandler;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        mBtDeviceManager = new BTDeviceManager(getContext(), adapter);
        mBtDeviceManager.setOnDeviceDiscoveryListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = null;// inflater.inflate(R.layout.dialog_bluetooth_list, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
//
//        rvBtDevicesList = (RecyclerView) view.findViewById(R.id.rv_bt_devices_list);
//        tvSelectedPrinter = (TextView) view.findViewById(R.id.tv_selected_printer);
//        tvMacAddress = (TextView) view.findViewById(R.id.tv_mac_address);
//        btnRemove = (Button) view.findViewById(R.id.btn_remove);

        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDefaultPrinter();
            }
        });

        rvBtDevicesList.setLayoutManager(new LinearLayoutManager(getContext()));

        mDevicesListAdapter = new BTDevicesListAdapter(getContext());
        mDevicesListAdapter.setEventListener(this);

        rvBtDevicesList.setAdapter(mDevicesListAdapter);

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            Window window = getDialog().getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
        if (mBtDeviceManager.isBluetoothOn()) {
            loadPairedDevices();
            rescan();
        } else {
            mBtDeviceManager.promptEnableBluetooth(getActivity(), this);
        }
        setPrinterInfoToUi();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQ_CODE_BT_REQUEST_ENABLE) {
            if (resultCode == RESULT_OK) {
                loadPairedDevices();
                rescan();
            }
        }
    }

    private void removeDefaultPrinter() {
//        SQLiteHelper.getInstance(getActivity()).clearSelectedDefaultPrinter();
        currentSelectedPrinter = null;
        setPrinterInfoToUi();
    }

    private void setPrinterInfoToUi() {

        if (currentSelectedPrinter != null) {
            tvSelectedPrinter.setText(currentSelectedPrinter.name);
            tvMacAddress.setText(currentSelectedPrinter.macAddress);
            btnRemove.setVisibility(View.VISIBLE);
        } else {
//            tvSelectedPrinter.setText(R.string.label_none);
            tvMacAddress.setText(null);
            btnRemove.setVisibility(View.GONE);
        }
    }


    private void loadPairedDevices() {
        Set<BluetoothDevice> devices = mBtDeviceManager.getPairedDevices();
        if (devices != null) {
            mDevicesListAdapter.addBluetoothDevices(devices);
        }
    }

    public void setCurrentSelectedPrinter(PrinterInfo info) {
        currentSelectedPrinter = info;
    }

    private void rescan() {
        mBtDeviceManager.startBluetoothDiscovery(DISCOVERY_DURATION_MS);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBtDeviceManager.stopBluetoothDiscovery();
    }

    @Override
    public void onItemClick(@NonNull BluetoothDevice device) {
        // On device click.. check if device is really a printer...
        // then connect to the device..
        //doPrinterActions(device);
        mBtDeviceManager.stopBluetoothDiscovery();
        if (mEventHandler != null) {
            mEventHandler.onDeviceSelected(device);

            dismiss();
        }
    }

    @Override
    public void onDiscoveryStarted() {
        mDevicesListAdapter.resetDevicesList();
    }

    @Override
    public void onDiscoveryFinished() {
        //
    }

    @Override
    public void onDeviceDiscovered(BluetoothDevice device) {
        mDevicesListAdapter.addBluetoothDevice(device);
    }

    public interface EventHandler {
        void onDeviceSelected(@NonNull BluetoothDevice device);
    }
}
