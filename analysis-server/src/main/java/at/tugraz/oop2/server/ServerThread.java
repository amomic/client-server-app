package at.tugraz.oop2.server;

import at.tugraz.oop2.Logger;
import at.tugraz.oop2.data.Sensor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


// TODO all the logic should be

public class ServerThread extends Thread {
    Socket socket;
    String path;
    List<Sensor> sensorList = new ArrayList<>();


    ServerThread(Socket socket, String path) {
        this.socket = socket;
        this.path = path;
    }

    @Override
    public void run() {
        super.run();
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            String msg = null;
            synchronized (inputStream) {
                msg = (String) inputStream.readObject();
            }
            if (msg.equals("queryLS")) {
                Logger.serverRequestLS();
                File file = new File(path + "/sensors");
                querySensors(file);
                outputStream.writeObject(sensorList);
                Logger.serverResponseLS(sensorList);

            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void querySensors(File file) throws IOException {
        boolean is_not_directory = false;
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                querySensors(f);
            } else {
                is_not_directory = true;
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                boolean ignoreFirst = true;
                while ((line = br.readLine()) != null) {
                    if (ignoreFirst) {
                        br.readLine();
                        ignoreFirst = false;
                    } else {
                        String[] objects = line.split(";",-1);
                        int id = Integer.parseInt(objects[0]);
                        String type = objects[1];
                        String location = objects[2];
                        double lat = Double.parseDouble(objects[3]);
                        double lon = Double.parseDouble(objects[4]);
                        String p1 = "";
                        String p2 = "";
                        if(type.equals("SDS011")) {
                            p1 = "P1";
                            p2 = "P2";
                        }else if (type.equals("DHT22"))
                        {
                            p1 = "temperature";
                            p2 = "humidity";
                        } else if (type.equals("BME280"))
                        {
                            p1 = "temperature";
                            p2 = "humidity";
                        }

                        Sensor s1 = new Sensor(id, type, lat, lon, location, p1);
                        Sensor s2 = new Sensor(id, type, lat, lon, location, p2);
                        sensorList.add(s1);
                        sensorList.add(s2);
                        break;
                    }
                }
            }
            if(is_not_directory)
                break;
        }
    }
}