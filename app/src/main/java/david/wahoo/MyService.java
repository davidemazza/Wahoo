package david.wahoo;

        import android.app.Service;
        import android.content.Context;
        import android.content.Intent;
        import android.database.sqlite.SQLiteDatabase;
        import android.hardware.Sensor;
        import android.hardware.SensorEvent;
        import android.hardware.SensorEventListener;
        import android.hardware.SensorManager;
        import android.os.IBinder;
        import android.util.Log;

        import com.wahoofitness.connector.*;
        import com.wahoofitness.connector.HardwareConnectorEnums;
        import com.wahoofitness.connector.HardwareConnectorTypes;
        import com.wahoofitness.connector.capabilities.Capability;
        import com.wahoofitness.connector.capabilities.Heartrate;
        import com.wahoofitness.connector.conn.connections.SensorConnection;
        import com.wahoofitness.connector.conn.connections.params.ConnectionParams;
        import com.wahoofitness.connector.listeners.discovery.DiscoveryListener;

        import java.sql.Timestamp;
        import java.util.Timer;
        import java.util.TimerTask;



class MyService extends Service {

    private Timer timer;
    private HardwareConnector mHardwareConnector;
    boolean cardiofreqConnesso;
    private SensorConnection sensorConn;
    private SensorManager mSensorManager;
    private Sensor sensoreInternoPassi;
    private int passiAttuali;
    private SensorConnection.Listener listenSensorConn = new SensorConnection.Listener() {
        public void onNewCapabilityDetected(SensorConnection sensorConnection, Capability.CapabilityType capabilityType) {
        }

        public void onSensorConnectionError(SensorConnection sensorConnection, HardwareConnectorEnums.SensorConnectionError sensorConnectionError) {
            System.out.println("aaaaaaaaaaaaaaaa listener2" + "     " + sensorConnectionError);
            if (sensorConnectionError == HardwareConnectorEnums.SensorConnectionError.BTLE_CONNECTION_LOST || sensorConnectionError == HardwareConnectorEnums.SensorConnectionError.BTLE_DISABLED || sensorConnectionError == HardwareConnectorEnums.SensorConnectionError.BTLE_INTERUPTED) {
                System.out.println("entrato nell if della connessione persa");
                mHardwareConnector.startDiscovery(mDiscoveryListener);
                cardiofreqConnesso = false;
            }


        }

        public void onSensorConnectionStateChanged(SensorConnection sensorConnection, HardwareConnectorEnums.SensorConnectionState sensorConnectionState) {
        }
    };


    private final DiscoveryListener mDiscoveryListener = new DiscoveryListener() {

        @Override
        public void onDeviceDiscovered(ConnectionParams params) {
            Log.i("TEST", "CONNESSO");
            sensorConn = mHardwareConnector.requestSensorConnection(params, null);
            sensorConn.addListener(listenSensorConn);
            cardiofreqConnesso = true;
            mHardwareConnector.stopDiscovery();
        }

        @Override
        public void onDiscoveredDeviceLost(ConnectionParams params) { // è quando si disconnette??
            cardiofreqConnesso = false;
            Log.i("TEST", "DISCCONNESSO");

            sensorConn = null;
        }

        @Override
        public void onDiscoveredDeviceRssiChanged(ConnectionParams params, int rssi) {
        }
    };


    private final HardwareConnector.Listener mHardwareConnectorListener = new HardwareConnector.Listener() {

        @Override// DEPRECATO!!
        public void connectedSensor(SensorConnection sensorConnection) {
            Log.i("TEST", "CONNESSO 2 ");
            cardiofreqConnesso = true;

        }

        @Override
        public void connectorStateChanged(HardwareConnectorTypes.NetworkType networkType,
                                          HardwareConnectorEnums.HardwareConnectorState hardwareState) {
        }

        @Override  // DEPRECATO!!
        public void disconnectedSensor(SensorConnection sensorConnection) {

        }

        @Override
        public void onFirmwareUpdateRequired(SensorConnection sensorConnection,
                                             String currentVersionNumber, String recommendedVersion) {
        }
    };

    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            double battito = 0;

            Log.i("TEST", "Inizio! \n Battito:  " + battito);

            if (cardiofreqConnesso) {
                Log.i("TEST", "CONNESSO 3 ");

                if (sensorConn != null) {
                    Log.i("TEST", "SENSORE != NULL ");

                    Heartrate heartrate = (Heartrate) sensorConn.getCurrentCapability(Capability.CapabilityType.Heartrate);
                    Log.i("TEST", "HEARTRATE: " + heartrate);
                    if (heartrate != null) {
                        battito = heartrate.getHeartrateData().getHeartrate().asEventsPerMinute();
                        Log.i("TEST", "Battito:  " + battito);
                        heartrate.resetHeartrateData();
                    }
                    Log.i("TEST", "PASSATO HEARTRATE != NULL");
                }
            }
            Record record = new Record(new Timestamp(System.currentTimeMillis()));
            Log.i("TEST", "Passi: " + passiAttuali);
            record.setNumeroPassi(passiAttuali);
            passiAttuali = 0;
            record.setPulsazioniCardiache((int) battito);

        }
    };


    @Override
    public void onCreate() {
        super.onCreate();

        passiAttuali = 0;

            mHardwareConnector = new HardwareConnector(this, mHardwareConnectorListener);
            mHardwareConnector.startDiscovery(mDiscoveryListener);
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
                sensoreInternoPassi = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
                mSensorManager.registerListener(listenerContaPassiInterno, sensoreInternoPassi, SensorManager.SENSOR_DELAY_NORMAL);
            }

            timer = new Timer("Scheduler");
            timer.schedule(updateTask, 0, 10 * 1000L);
        }


    private SensorEventListener listenerContaPassiInterno = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent e) {
            if (e.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                passiAttuali++;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
    }

    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
