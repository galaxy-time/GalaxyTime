package jp.lab75.galaxytime.sensor// package jp.lab75.galaxytime

// import android.hardware.Sensor
// import android.hardware.SensorEvent
// import android.hardware.SensorEventListener
// import android.hardware.SensorManager

// class SensorDataHandler(
// 	private val sensorManager: SensorManager,
// 	private val sensorType: Int
// ) : SensorEventListener {

//     var sensor: Sensor? = null
//     var sensorData: FloatArray? = null

//     init {
//         sensor = sensorManager.getDefaultSensor(sensorType)
//     }

//     override fun onSensorChanged(event: SensorEvent) {
//         if (event.sensor.type == sensorType) {
//             sensorData = event.values
//         }
//     }

//     override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
//         // Do nothing
//     }

//     fun getSensorData(): FloatArray? {
//         return sensorData
//     }
// }
