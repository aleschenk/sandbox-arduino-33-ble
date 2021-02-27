// Modules to control application life and create native browser window
const {app, BrowserWindow, ipcMain} = require('electron')
const path = require('path')
const SerialPort = require('serialport')
const Readline = require('@serialport/parser-readline')
// import Duplex from ('stream').Duplex
var sp

// get environment type
// const isDevelopment = process.env.NODE_ENV !== 'production';

const createWindow = () => {
  // Create the browser window.
  const mainWindow = new BrowserWindow({
    width: 1024,
    height: 768,
    webPreferences: {
      preload: path.join(__dirname, '/renderer/preload.js'),
      nodeIntegration: true,
      // allowRunningInsecureContent: true,
      // contextIsolation: false
    },
  })

  console.log("OS ProcessID: " + mainWindow.webContents.getOSProcessId())
  console.log("ProcessID: " + mainWindow.webContents.getProcessId())

  // and load the index.html of the app.
  // mainWindow.loadFile('index.html')
  mainWindow.loadFile(path.resolve(__dirname, 'renderer/index.html'));

  // load HTML file
  // if (isDevelopment) {
  //   win.loadURL(`http://${process.env.ELECTRON_WEBPACK_WDS_HOST}:${process.env.ELECTRON_WEBPACK_WDS_PORT}`);
  // } else {
  //   win.loadFile(path.resolve(__dirname, 'index.html'));
  // }

  // Open the DevTools.
  // mainWindow.webContents.openDevTools()
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
  createWindow()

  app.on('activate', function () {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', function () {
  if (process.platform !== 'darwin') app.quit()
})

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and require them here.
ipcMain.handle('sp:get-ports', (event) => {
  return SerialPort.list()
})

ipcMain.on('sp:connect', (event, port) => {
  sp = new SerialPort(port, {
    baudRate: 9600
  })

  const parser = sp.pipe(new Readline({delimiter: '\n'}));
  parser.on('data', (data) => {
    console.log(data)
    event.sender.send('sp:data', data)
  });
})

ipcMain.on('sp:disconnect', () => {
  sp.close()
})

ipcMain.on('sp:test', (event) => {
  setInterval((event) => {
    var temp = Math.floor(Math.random() * 30) + 25
    var pressure = Math.floor(Math.random() * 101) + 100
    var humidity = Math.floor(Math.random() * 60) + 50
    var x = Math.floor(Math.random() * 255) + 0
    var y = Math.floor(Math.random() * 255) + 0
    var z = Math.floor(Math.random() * 255) + 0
    var r = Math.floor(Math.random() * 255) + 0
    var g = Math.floor(Math.random() * 255) + 0
    var b = Math.floor(Math.random() * 255) + 0
    var ge = Math.floor(Math.random() * 4) + -1

    event.sender.send('sp:data',
      temp + ',' + pressure + ',' + humidity + ',' + x + ',' + y + ',' + z + ',' + r + ',' + g + ',' + b + ',' + ge
    )
  }, 100, event)
})