/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cellularautomata;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author Thomas & Kevin
 */
public class CelularAutomata extends JFrame
{
    private final String OS;    
    private final JPanel worldPanel;
    private int[][] land;
    private final int width = 1000;
    private final int height = 15;
    private int blockSize = 10;
    
    public CelularAutomata()
    {
        OS = System.getProperty("os.name").toLowerCase();
        
        this.land = new int[this.width][this.height];
        worldPanel = new JPanel() 
        {
            @Override
            public void paint(Graphics g)
            {
                if(land != null)
                {
                    for(int i = 0; i < width; i++)
                    {
                        for(int j = 0; j < height; j++)
                        {
                            switch(land[i][j])
                            {
                                case 0:
                                    g.setColor(Color.WHITE);
                                    break;
                                case 1:
                                    g.setColor(Color.BLUE);
                                    break;
                                case 2:
                                    g.setColor(Color.YELLOW);
                                    break;
                                case 3:
                                    g.setColor(Color.CYAN);
                                    break;
                                case 4:
                                    g.setColor(Color.PINK);
                                    break;
                            }
                            
                            g.fillRect(blockSize*i, blockSize*j, blockSize, blockSize);
                        }
                    }
                }
            }
        };
        
        if(OS.contains("win"))
        {
            this.setSize(1020 + 16, height * this.blockSize + 58);
        }else if(OS.contains("mac"))
        {
            this.setSize(1020, height * this.blockSize + 41);
        }
        
        worldPanel.setPreferredSize(new Dimension(width * this.blockSize, height * this.blockSize));
        JScrollPane scrollPane = new JScrollPane(worldPanel);
        worldPanel.setAutoscrolls(true);
        scrollPane.setPreferredSize(new Dimension(1000, height * this.blockSize));
        
        this.add(scrollPane);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        
        this.generateLand();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        CelularAutomata test = new CelularAutomata();
        test.processN(10);
        test.placeCoins();
        test.landCleanUp();
        test.createWalls();
        test.makePlatforms();
        test.makeFloor();
        test.repaint();
    }
    
    public void processN(int n) 
    {
        //*
        for(int i = 0;i < n; i++)
        {
            //land = process(land);
            //generateLand();    
            land = updateLand(land);
            
            try {
                    Thread.sleep(750);
            } catch (InterruptedException e) {
                    e.printStackTrace();
            }
            repaint();
        }
        //*/
    }
    
    private int[][] updateLand(int[][] land)
    {
        //int[][] neighboors = neighboorsLand(land);
        int[][] nextTo = nextToLand(land, 1);
        int[][] topBottom = betweenLand(land, 1);
        int[][] temp = land;
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                //rule one (no neighboors become air)
                if(nextTo[i][j] == 0)
                {
                    temp[i][j] = 0;
                } else if(nextTo[i][j] == 2)
                { //rule two (block needs to be at least 3)
                        temp[i][j] = 1;
                } else 
                {
                    //*
                    if(land[i][j] == 1)
                    {
                        if(nextTo[i][j] == 1)
                        {
                            if(i > 0 && nextTo[i-1][j] == 2)
                            {
                                temp[i][j] = 1;
                            } else if(i < width-1 && nextTo[i+1][j] == 2)
                            {
                                temp[i][j] = 1;
                            } else 
                            {
                                temp[i][j] = 0;
                            }
                        }
                    }
                    //*/
                }
                
                // rule three (when a bock has no block above and below look at block above it that one has two topbottom neighboors.
                // if it has remove the origional block)
                if(land[i][j] == 1 && topBottom[i][j] == 0)
                {
                    if(j > 0 && topBottom[i][j-1] == 2)
                    {
                         temp[i][j] = 0;
                    }
                }
                
                // rule five (if air has 1 neighboor below with a solid neighboor then remove this first neighboor below the air)
                if(land[i][j] == 0 && topBottom[i][j] == 1)
                {
                    if(j > height-1 && land[i][j+1] == 1 && topBottom[i][j+1] == 1)
                    {
                        temp[i][j+1] = 0;
                    }
                }
            }
        }
        return temp;
        
    }
    
    private int[][] nextToLand(int[][] land, int tile)
    {
        int[][] temp = new int[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                temp[i][j] = 0;
                if(i>0)
                {
                    temp[i][j] += (land[i-1][j] == tile) ? 1 : 0; // left
                }
                if(i < width - 1)
                {
                    temp[i][j] += (land[i+1][j] == tile) ? 1 : 0; // right
                }
            }
        }
        return temp;
    }
    
    private int[][] betweenLand(int[][] land, int tile)
    {
        int[][] temp = new int[width][height];
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                temp[i][j] = 0;
                if(j>0)
                {
                    temp[i][j] += (land[i][j-1] == tile) ? 1 : 0; // top
                }
                if(j < height - 1)
                {
                    temp[i][j] += (land[i][j+1] == tile) ? 1 : 0; // bottom
                }
            }
        }
        return temp;
    }
    
    private void placeCoins() 
    {
        int[][] topBottom = betweenLand(land, 1);
        for(int i = 0; i < width - 1; i++)
        {
            for(int j = 0; j < height -1; j++)
            {
                if(land[i][j] == 1 && topBottom[i][j] == 0)
                {
                    if(j < height - 2 && land[i][j+2] == 1)
                    {
                         land[i][j] = 2;
                    }
                }
            }
        }
    }
    
    private void makePlatforms()
    {
        int[][] topBottom = betweenLand(land, 3);
        for(int i = 0; i < width-1; i++)
        {
            for(int j = 0; j < height - 3; j++)
            {
                if(topBottom[i][j] == 0 && land[i][j] == 1)
                {
                    land[i][j] = 4;
                }
            }
        }
    }
    
    private int[][] landCleanUp()
    {
        int[][] temp = land;
        int[][] topBottom = betweenLand(land,1);
        int[][] neighboors = nextToLand(land,1);
        
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                //*
                // rule foor (when air is enclosed in blocks fill the air)
                if(land[i][j] == 0 && topBottom[i][j] == 2 )
                {//&& ((i > 0 && topBottom[i-1][j] == 1) || (i < (land.length - 2) && topBottom[i+1][j] == 1))){
                    temp[i][j-1] = 1;
                    temp[i][j] = 1;
                    temp[i][j+1] = 1;
                }
                //*/
                
                //*
                if(land[i][j] == 1 && neighboors[i][j] == 0)
                {
                    temp[i][j] = 0;
                }
                //*/
                
                //*
                // place wall below platform if platform is 2 thick
                if(land[i][j] == 1 && (topBottom[i][j] > 0 || land[i][j] == 3))
                {
                    if(j > 0 && (land[i][j-1] == 3 || land[i][j-1] == 1))
                    {
                        land[i][j] = 3;
                    }
                }
                if(land[i][j] == 0 && j > 0 && land[i][j-1] == 3)
                {
                    land[i][j] = 3;
                }
                //*/
            }
        }
        
        return temp;
    }
    
    private void createWalls()
    {
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                //*
                // place wall below platform in bottom x blocks of level if the floor has only air below
                if(j > (height - 10) && land[i][j] == 1)
                {
                    if(checkAir(i,j))
                    {
                        setWall(i,j);
                    }
                }
                //*/
            }
        }         
    }
    
    /*
     * Helper Function check for only air below a block
     */
    private boolean checkAir(int i, int j)
    {
        boolean result = true;
        for(int x = (j+1); x < height; x++)
        {
            if(land[i][x] == 1)
            {
                result = false;
            }
        }
        
        return result;
    }
    
    /*
     * Helper Function place wall below block till botom of world
     */
    private void setWall(int i, int j)
    {
        for(int x = (j+1); x < height; x++)
        {
            land[i][x] = 3;
        }
    }
    
    private void makeFloor()
    {
        int count = 0;
        int floorHeight = 0;
        
        for(int i = 0; (i < width && i < 20); i++)
        {
            for(int j = 0; j < height; j++)
            {
                if(land[i][j] == 1)
                {
                    floorHeight += i;
                    count++;
                }
            }
        }
        
        floorHeight = floorHeight/count + 5;
        if(floorHeight > (height - 1))
        {
            floorHeight = height - 2;
        }
        
        for(int i = 0; (i < width && i < 20); i++)
        {
            for(int j = 0; j < height; j++)
            {
                if(j > floorHeight)
                {
                    land[i][j] = 3;
                } else if(j == floorHeight)
                {
                    land[i][j] = 1;
                } else 
                {
                    land[i][j] = 0;
                }
            }
        }
    }
    

    
//    private int[][] neighboorsLand(int[][] land){
//        int[][] temp = new int[width][height];
//        for(int i = 0; i < width; i++){
//            for(int j = 0; j < height; j++){
//                temp[i][j] = 0;
//                if(j>0){
//                    temp[i][j] += land[i-1][j]; // left
//                    if(i>0){
//                        temp[i][j] += land[i-1][j-1]; // top-left
//                    }
//                    if(i<land.length - 1){
//                        temp[i][j] += land[i-1][j+1]; // bottom-left
//                    }
//                }
//                if(j < land[0].length - 1){
//                    temp[i][j] += land[i+1][j]; // right
//                    if(i>0){
//                        temp[i][j] += land[i+1][j-1]; // top-right
//                    }
//                    if(i<land.length - 1){
//                        temp[i][j] += land[i+1][j+1]; // bottom-right
//                    }
//                }
//                if(i>0){
//                    temp[i][j] += land[i][j-1]; // top
//                }
//                if(i<land.length - 1){
//                    temp[i][j] += land[i][j+1]; // bottom
//                }
//            }
//        }
//        return temp;
//    }
    
    private void generateLand()
    {
        if(land != null)
        {
            for(int i = 0; i < width; i++)
            {
                for(int j = 0; j < height; j++)
                {
                    land[i][j] = ((Math.random() + ((double)j/(height*1.7))) > 0.90 ? 1 : 0);
                }
            }
        }
    }
}