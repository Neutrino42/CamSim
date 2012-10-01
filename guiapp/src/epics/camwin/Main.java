/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package epics.camwin;

import epics.camsim.core.CameraController;
import epics.camsim.core.SimCore;
import epics.camsim.core.SimSettings;
import epics.camsim.core.TraceableObject;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author Lukas Esterle <Lukas.Esterle@aau.at> & Marcin Bogdanski <mxb039@cs.bham.ac.uk>
 */
public class Main {

    // Input XML file with simulation definition
    static String input_file = null;
    // Output file with all simulation statistics
    static String output_file = "output.csv";
    // Seed
    static long seed = 0;
    // After this many timesteps simulation will stop
    static int simulation_time = 100;
    // No-gui, set this to false to disable gui.
    static boolean showgui = true;
    
    static boolean useGlobal = false;
    
    static String algo = "";
    static String comm = "";
    static int predefVG = -1;

    static void print_parameters() {

        /*
         * Nice dashed line on top
         */
        for (int i = 0; i < 28; i++) {
            System.out.print('-');
        }
        System.out.print(" Simulation Parameters ");
        for (int i = 0; i < 29; i++) {
            System.out.print('-');
        }
        System.out.println();

        System.out.println("Input file:      " + input_file);
        System.out.println("Output file:     " + output_file);
        System.out.println("Seed:            " + seed);
        System.out.println("Simulation time: " + simulation_time);

        /*
         * Nice dashed line on the bottom as well
         */
        for (int i = 0; i < 80; i++) {
            System.out.print('-');
        }
        System.out.println();


    }

    public static void usage() {
        System.out.println("USAGE: ");
        System.out.println("  program [OPTIONS] input_file");
        System.out.println("\nuse -h vor help");
    }

    public static void help() {

        usage();

        /*
         * TODO: implement help
         */
        System.out.println(
                
                "OPTIONS:\n" +
                " -h --help              Print this help message\n" +
                " -o --output [STRING]   Change output file name (default: output.csv)\n" +
                " -s --seed [INTEGER]    Used this seed (default: 0)\n" +
                " -t --time [INTEGER]    Simulation time, in time steps (default: 100)\n" +
                " -g --global            Uses Global Registration Component\n" +
                " -v --vg [INTEGER]		 defines the visiongraph ((default) -1 = defined in scenario file, 0 = static as defined in scenario, 1 = dynamic - ignore scenario file, 2 = dynamic - start with scenario file \n" +
                " -c --comm [INTEGER] 	 Defines Communication ((default) 0 = Broadcast, 1 = SMOOTH, 2 = STEP, 3 = Static) \n" +
                " -a --algo [STRING]	 Defines the used algorithm ((default) \"active\", \"passive\") \n" +
                "\n" +
                "    --no-gui            Will launch simulator in command line mode\n"

                );
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        /*
         * Set the parameters depending on command line options.
         */

        int c;
        String arg;
        LongOpt[] longopts = new LongOpt[9];

        StringBuffer sb = new StringBuffer();
        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
        longopts[2] = new LongOpt("seed", LongOpt.REQUIRED_ARGUMENT, null, 's');
        longopts[3] = new LongOpt("time", LongOpt.REQUIRED_ARGUMENT, null, 't');
        longopts[4] = new LongOpt("no-gui", LongOpt.NO_ARGUMENT, null, 1000);
        longopts[5] = new LongOpt("global", LongOpt.NO_ARGUMENT, null, 'g');
        longopts[6] = new LongOpt("algo", LongOpt.REQUIRED_ARGUMENT, null, 'a');
        longopts[7] = new LongOpt("comm", LongOpt.REQUIRED_ARGUMENT, null, 'c');
        longopts[8] = new LongOpt("vg", LongOpt.REQUIRED_ARGUMENT, null, 'v');
        
        Getopt g = new Getopt("guiapp", args, "a:c:v:gho:s:t:", longopts);
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 0:
                    arg = g.getOptarg();
                    System.out.println("Got long option with value '"
                            + (char) (new Integer(sb.toString())).intValue()
                            + "' with argument "
                            + ((arg != null) ? arg : "null"));

                    break;
                //
                case 1:
                    System.out.println("I see you have return in order set and that "
                            + "a non-option argv element was just found "
                            + "with the value '" + g.getOptarg() + "'");
                    break;
                //

                case 'h':
                    help();
                    System.exit(0);
                    break;
                //

                case 'o':
                    arg = g.getOptarg();
                    System.out.println("Setting output file to: " + arg);
                    output_file = arg;
                    break;

                case 's':
                    arg = g.getOptarg();
                    try {
                        seed = Integer.parseInt(arg);
                    } catch (NumberFormatException e) {
                        System.err.println(
                                "Value passed as seed is not a vaild integer: "
                                + arg + " i");
                        System.exit(1);
                    }
                    break;

                case 't':
                    arg = g.getOptarg();
                    try {
                        simulation_time = Integer.parseInt(arg);
                    } catch (NumberFormatException e) {
                        System.err.println(
                                "Value passed as time is not a vaild integer: "
                                + arg + " i");
                        System.exit(1);
                    }
                    break;
                case 'g':
                	useGlobal = true;
                	break;
                case 'a':
                	arg = g.getOptarg();
                	algo = arg;
                	break;
                case 'c':
                	arg = g.getOptarg();
                	comm = arg;
                	break;
                case 'v':
                	arg = g.getOptarg();
                	predefVG = Integer.parseInt(arg);
                	break;
                case 1000: // no-gui
                    showgui = false;
                    break;

                case ':':
                    System.out.println("Doh! You need an argument for option "
                            + (char) g.getOptopt());
                    break;
                //
                case '?':
                    System.out.println("The option '" + (char) g.getOptopt()
                            + "' is not valid");
                    break;
                //
                default:
                    System.out.println("getopt() returned " + c);
                    break;
            }
        }

        int opt_arg_counter = 0;
        StringBuilder strb = new StringBuilder();
        String filename = null;
        for (int i = g.getOptind(); i < args.length; i++) {
            opt_arg_counter++;
            strb.append(args[i]);
            System.out.println("Non option argv element: " + args[i] + "\n");
        }

        if (opt_arg_counter > 1) {
            System.err.println("Error, I don't understand these parameters '"
                    + strb.toString() + "'");
            usage();
            System.exit(1);
        }

        if (opt_arg_counter == 1) {
            input_file = strb.toString();
        }

        print_parameters();
        SimSettings ss = new SimSettings(algo, comm, predefVG);
        if (input_file == null) {
            System.err.println("Error, no simulation file provided");
            usage();
            System.exit(1);
        	
        }
        else{
	        
	        boolean success = ss.loadFromXML(input_file);
	
	        if (!success) {
	            System.err.println("Error, Could not load " + input_file);
	            System.exit(1);
	        }
        }
        if (showgui == false) {

            SimCore sim = new SimCore(seed, output_file, ss, useGlobal);

            for (int i = 0; i < simulation_time; i++) {
                sim.update();
            }

            sim.close_files();

        } else {

        	SimCore sim = new SimCore(seed, output_file, ss, useGlobal);
            sim_model = new SimCoreModel(sim);
            WindowMain win = new WindowMain(sim_model, input_file);
            win.createAndShowGUI();
        }

    }

    public static SimCoreModel sim_model;

}