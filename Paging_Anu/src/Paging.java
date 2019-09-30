import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import java.awt.*;

import java.io.*;
import java.util.Random;
import java.util.Scanner;


public class Paging
{

    private static int[][] frame;
    private static int NumofFaults=0;
    //Making a text file for the graph
    public static void GeneratePageFrames(){
        try{
            //Establishing  random integers from 1-10 to make the function
            FileWriter fw = new FileWriter("Data.txt");
            Random r1 = new Random();
            for(int i = 0;i<1000;i++){
                int n = r1.nextInt(10) +1;
                fw.write(String.valueOf(n)+",");
            }
            fw.close();
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
    public static void main(String[] args) throws IOException
    {
        GeneratePageFrames();
        // Reading a file and storing the page references in an array
        File file=new File("Data.txt");

        FileReader f=new FileReader(file);
        BufferedReader b = new BufferedReader(f);

        String line=null;
        String[] pageRef=null;

        while((line=b.readLine())!=null)
        {
            pageRef = line.trim().split(",");
        }
        b.close();

        Scanner s=new Scanner(System.in);
        // Number of executions that are to be plotted on the graph. This oes on X axis
        System.out.print("Enter the Number of Cases : ");
        int inputs=s.nextInt();
        // To store execution in a 2D array, number of frames, number of page faults , page faults per 1000 references
        double[][] pageTable=new double[inputs][4];

        for (int i = 0; i < inputs; i++)
        {

            System.out.print("\nEnter the Number of Page Frames for Case "+(i+1)+" : ");
            int pageFrames=s.nextInt();
            frame=new int[pageFrames][2];

            for (int x = 0; x < frame.length; x++)
            {
                frame[x][0]=-1;
                frame[x][1]=0;
            }

            Paging pag=new Paging();

            for (int j = 0; j < pageRef.length; j++)
            {
                //Aging algorithm
                pag.agingAlgo(Integer.parseInt(pageRef[j]));
            }

            pageTable[i][0]=pageFrames;
            pageTable[i][1]=NumofFaults;
            pageTable[i][2]=NumofFaults*1000/(double)(pageRef.length);

            System.out.println("Total Page Faults for cases"+(i+1)+" is "+NumofFaults);

            NumofFaults=0;
            frame=null;
        }

        System.out.println();
        String formatter="%20s\t%20s\t%50s\n";
        System.out.format(formatter, "Number of PageFrames","Total PageFaults","Number of Page Faults/1000 Memory Reference");

        int[] x=new int[pageTable.length];
        double[] y=new double[pageTable.length];

        for (int i = 0; i < pageTable.length; i++)
        {
            System.out.format(formatter, (int)pageTable[i][0],(int)pageTable[i][1],pageTable[i][2]);
            x[i]=(int) pageTable[i][0];
            y[i]= pageTable[i][2];
        }

        s.close();
        //Plot graph
        graph(x,y);
    }

    private static void graph(int[] x,double[] y) {

        XYgraph chart = new XYgraph("XY Graph", "Page Fault Rate","Number of Frames","Page Fault/1000 Memory Ref.","Fault Rate",x,y);
        chart.pack( );
        RefineryUtilities.centerFrameOnScreen( chart );
        chart.setVisible( true );
    }

    private void agingAlgo(int pageNumber){

        boolean isPageAssigned=false;
        //Initially pushing pages into frame.
        for (int i = 0; i < frame.length; i++)
        {
            frame[i][1]=frame[i][1]>>1;
            if(frame[i][0]==pageNumber)
            {
                isPageAssigned=true;
                frame[i][0] |= 1 << 32;
            }
        }
        //If page is not in frame, increase the page fault and add it to the frame
        if(!isPageAssigned)
        {
            NumofFaults++;
            for (int i = 0; i < frame.length; i++)
            {
                if(frame[i][0]==-1)
                {
                    frame[i][0]=pageNumber;
                    isPageAssigned=true;
                    frame[i][1]=1;
                    break;
                }
            }

            if(!isPageAssigned)
            {
                int minCount=frame[0][1];
                int minCountIndex=0;
                for (int i = (frame.length-1); i >= 0 ; i--)
                {
                    if(frame[i][1]<minCount)
                    {
                        minCount=frame[i][1];
                        minCountIndex=i;
                    }
                }

                frame[minCountIndex][0]=pageNumber;
                frame[minCountIndex][1]=1;
                isPageAssigned=true;
            }
        }
    }
}

//The class is used to plot the graph
class XYgraph extends ApplicationFrame
{

    public XYgraph( String applicationTitle, String graphTitle,String xLine,String yLine,String datasetName,int[] x,double[] y)
    {
        super(applicationTitle);
        JFreeChart xyGraph = ChartFactory.createXYLineChart(
                graphTitle ,
                xLine,
                yLine,
                createDataset(datasetName,x,y) ,
                PlotOrientation.VERTICAL ,
                true , true , false);

        ChartPanel chartPanel = new ChartPanel( xyGraph );
        chartPanel.setPreferredSize( new java.awt.Dimension( 500 , 350 ) );
        final XYPlot plot = xyGraph.getXYPlot( );
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
        renderer.setSeriesPaint( 0 , Color.BLUE );
        plot.setRenderer( renderer );
        setContentPane( chartPanel );
    }

    private XYDataset createDataset(String datasetName, int[] x, double[] y){
        final XYSeries faultRate = new XYSeries(datasetName);

        for (int i = 0; i < x.length; i++)
        {
            faultRate.add( x[i] , y[i]);
        }

        final XYSeriesCollection dataset = new XYSeriesCollection( );
        dataset.addSeries( faultRate );
        return dataset;
    }
}