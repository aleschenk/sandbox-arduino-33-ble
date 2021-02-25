var blessed = require('blessed')
var contrib = require('blessed-contrib')

var screen = blessed.screen()

var grid = new contrib.grid({rows: 6, cols: 2, screen: screen})

var options = {
    label: "Temperature - Chart",
    style:
        {
            line: "yellow"
            , text: "green"
            , baseline: "black"
        }
    , xLabelPadding: 1
    , xPadding: 1
};

data = {
    x: ['t1', 't2', 't3', 't4'],
    y: [5, 1, 7, 5]
}

// screen.append(line)

// var humidityChart = grid.set(0, 0, 2, 2, contrib.line, options)


// Temperature Sensor
var temperatureValue = grid.set(0, 0, 1, 1, blessed.box, {label: 'Temperature - Value'})
var temperatureChart = grid.set(0, 1, 1, 1, contrib.line, options)

// Relative Humidity Sensor
var humidityValue = grid.set(1, 0, 1, 1, blessed.box, {label: 'Humidity - Value'})
var humidityChart = grid.set(1, 1, 1, 1, blessed.box, {label: "Humidity - Chart"})

// Pressure Sensor
var pressureValue = grid.set(2, 0, 1, 1, blessed.box, {label: 'Pressure - Value'})
var pressureChart = grid.set(2, 1, 1, 1, blessed.box, {label: "Pressure - Chart"})

// Proximity Sensor
var proximityValue = grid.set(3, 0, 1, 1, blessed.box, {label: 'Proximity - Value'})
var proximityChart = grid.set(3, 1, 1, 1, blessed.box, {label: "Proximity - Chart"})

// Gesture Sensor
var gestureValue = grid.set(4, 0, 1, 1, blessed.box, {label: 'Gesture - Value'})
var gestureChart = grid.set(4, 1, 1, 1, blessed.box, {label: "Gesture - Chart"})

var imuChart = grid.set(5, 0, 1, 2, blessed.box, {label: "IMU - Chart"})

// light intensity 
temperatureChart.setData([data])
screen.render()