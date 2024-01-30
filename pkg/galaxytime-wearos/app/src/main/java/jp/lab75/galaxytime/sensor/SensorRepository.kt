package jp.lab75.galaxytime.sensor// package jp.lab75.galaxytime

// import android.content.Context
// import android.hardware.Sensor
// import android.hardware.SensorManager
// import androidx.lifecycle.LiveData
// import androidx.lifecycle.MutableLiveData

// class SensorRepository(private val context: Context) {

//     private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//     private val sensorDataHandler = SensorDataHandler(sensorManager, Sensor.TYPE_ACCELEROMETER)

//     private val sensorDataLiveData = MutableLiveData<FloatArray>()

//     init {
//         sensorManager.registerListener(sensorDataHandler, sensorDataHandler.sensor, SensorManager.SENSOR_DELAY_NORMAL)
//     }

//     fun getSensorDataLiveData(): LiveData<FloatArray> {
//         return sensorDataLiveData
//     }

//     fun getSensorData(): FloatArray? {
//         return sensorDataHandler.getSensorData()
//     }
// }
