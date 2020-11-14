package at.tugraz.oop2.server;

import at.tugraz.oop2.Logger;
import at.tugraz.oop2.data.Sensor;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


// TODO all the logic should be

public class ProjectThread extends Thread {
    Socket socket;
    String path;
    List<Sensor> sensorList = new ArrayList<>();


    ProjectThread(Socket socket, String path) {
        this.socket = socket;
        this.path = path;
    }

    @Override
    public void run() {
        super.run();
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            String msg = (String) inputStream.readObject();
            if (msg.equals("queryLS")) {

                Logger.err("we got something from a client which is : " + msg);
                File file = new File(path);
                System.out.println(file.getAbsolutePath());
                querySensors(file);
                outputStream.writeObject(sensorList);

            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    private void querySensors(File file) throws IOException {
        System.out.println("start");
            for (File f : file.listFiles()) {
                System.out.println(f.getName());
                if (f.isDirectory()) {
                    querySensors(f);
                } else {
                    BufferedReader br = new BufferedReader(new FileReader(f));
                    String line;
                    boolean ignoreFirst = true;
                    while ((line = br.readLine()) != null) {
                        if (ignoreFirst) {
                            br.readLine();
                            ignoreFirst = false;
                        } else {
                            String[] objects = line.split(";");
                            int id = Integer.parseInt(objects[0]);
                            String type = objects[1];
                            String location = objects[2];
                            double lat = Double.parseDouble(objects[3]);
                            double lon = Double.parseDouble(objects[4]);
                            String p1 = objects[6];
                            String p2 = objects[9];
                            Sensor s1 = new Sensor(id, type, lat, lon, location, p1);
                            Sensor s2 = new Sensor(id, type, lat, lon, location, p2);
                            sensorList.add(s1);
                            sensorList.add(s2);
                        }

                    }
                }
            }
        }


}