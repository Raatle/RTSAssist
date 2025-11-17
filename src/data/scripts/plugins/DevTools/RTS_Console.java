/****************************************************************************************
 * RTSAssist version 0.1.5
 * Copyright (C) 2025, Raatle

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/gpl-3.0.en.html.
 ****************************************************************************************/

package data.scripts.plugins.DevTools;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RTS_Console {

    public RTS_Console () {
        newProcess process = new newProcess();
        process.start();
    }

    public class newProcess extends Thread {

        @Override
        public void run() {
            new SimpleExample();
        }
    }

    public class SimpleExample extends Frame {
        SimpleExample(){
            Button b=new Button("Button");
            b.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("test");
                }
            });
            b.setBounds(50,50,50,50);
            add(b);
            setSize(500,300);
            setTitle("THIS IS MY FIRST AWT EXAMPLE");
            setLayout(new FlowLayout());
            setVisible(true);
        }
    }

}