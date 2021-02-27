// This file is required by the index.html file and will
// be executed in the renderer process for that window.
// No Node.js APIs are available in this process because
// `nodeIntegration` is turned off. Use `preload.js` to
// selectively enable features needed in the rendering
// process.

// let fs = require('fs')
// const serialport = require('serialport')
// const tableify = require('tableify')
const filename = "test.txt";
const {scaleLinear} = require('d3-scale')


const connect = (port) => {
  ipcRenderer.send("sp:connect", port)
}

const disconnect = () => {
  ipcRenderer.send("sp:disconnect")
}

const getports = () => {
  return ipcRenderer.invoke('sp:get-ports')
}

const test = () => {
  ipcRenderer.send("sp:test")
}

// ipcRenderer.on("sp:data", (event, data) => {
//   console.log(data)
// })

window.addEventListener('load', () => {
  // if (fs.existsSync(filename)) {
  //   document.getElementById("message").innerHTML = "The file " + filename + " does not exists.";
  //   return;
  // }

  // let data = fs.readFileSync(filename, 'utf8').split('\n')
  // data.forEach((contact, index))

  // Set a timeout that will check for new serialPorts every 2 seconds.
  // This timeout reschedules itself.
  // setTimeout(function listPorts() {
  //   listSerialPorts();
  //   setTimeout(listPorts, 2000);
  // }, 2000)
});


//   // ports.forEach(port => {
//   // console.log(port.path)
//   // port.locationId
//   // port.manufacturer
//   // port.path
//   // port.pnpId
//   // port.productId
//   // port.serialNumber
//   // port.vendorId
//   // })