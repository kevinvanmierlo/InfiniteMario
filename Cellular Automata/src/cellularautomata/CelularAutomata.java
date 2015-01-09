/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cellularautomata;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Random;
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
    private int[] floor;
    private final int width = 1000;
    private final int height = 15;
    private int blockSize = 10;
    
    public int beginFloorHeight = 0;
    
    public CelularAutomata()
    {
        OS = System.getProperty("os.name").toLowerCase();
        
        this.land = new int[this.width][this.height];
        this.floor = new int[this.width];
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
                                    if(floor[i] == j)
                                        g.setColor(Color.GREEN);
                                    else
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
                                case 5:
                                    g.setColor(Color.MAGENTA);
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
        test.removeDoubleBlocks();
        test.generatePlatforms();
        test.makeBreakablePlatforms();
        test.placeMissingWalls();
        test.repaint();
    }
    
    public void processN(int n) 
    {
        //*
        for(int i = 0;i < n; i++)
        {
            //land = process(land);
            //generateLand();   
            getFloor();
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
    
    /**
     * Use this to get the lowest tiles (floor)
     */
    private void getFloor()
    {
        for(int i = 0; i < width; i++)
        {
            int j = height - 1;
            while(land[i][j] != 1 && j > 0)// && j > (height * (3/5)))
            {
                j--;
            }
            if(j >0)//<= (height * (3/5)))
            {
                floor[i] = j;
            }else
            {
                floor[i] = -1;
            }
        }
    }
    
    /**
     * Use this to update each tile according to our rules
     * @param land The land containing all the tiles
     * @return The updated land
     */
    private int[][] updateLand(int[][] land)
    {
        //int[][] neighboors = neighboorsLand(land);
        int[][] nextTo = nextToLand(land, 1);
        int[][] topBottom = betweenLand(land, 1);
        int[][] temp = land;
        
        int gapWidth = 0;
        int startGap = 0;
        
        Random random = new Random();
        beginFloorHeight = random.nextInt(5);
        beginFloorHeight = height - 1 - beginFloorHeight;
        
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
                { //rule two continued
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
                
                // rule four (if air has 1 neighboor below with a solid neighboor then remove this first neighboor below the air)
                if(land[i][j] == 0 && topBottom[i][j] == 1)
                {
                    if(j > height-1 && land[i][j+1] == 1 && topBottom[i][j+1] == 1)
                    {
                        temp[i][j+1] = 0;
                    }
                }
                
//                if(land[i][j] == 1)
//                {
//                    if(j<height-3 && !checkNoSpecificTileBelow(i, j, 1) && land[i][j+1] != 1 && land[i][j+2] != 1 && land[i][j+3] != 1)
//                    {
//                        temp[i][j] = 0;
//                        temp[i][j+1] = 1;
//                    }
//                }
                
                // rule (two tiles above is air)
                if(floor[i] - 2 == j || floor[i] - 1 == j)
                {
                    temp[i][j] = 0;
                }
                
                // rule (if one of the top 3 tiles become air)
                if(j <= 3)
                {
                    temp[i][j] = 0;
                }
                
                // rule (make the beginning and ending of floor)
                if(i < 20 || i > width - 20)
                {
                    if(j == beginFloorHeight)
                    {
                        temp[i][j] = 1;
                        floor[i] = j;
                    }else
                    {
                        temp[i][j] = 0;
                    }
                }
            }
            
            // rule for gaps not to wide
            if(floor[i] != -1 && i < (width - 1))
            {
                temp = floorWithinJumpRange(startGap-1, i, temp, 2);
                
                if(gapWidth > 6)
                {
                    temp[startGap][floor[startGap-1]] = 1;
                    floor[startGap] = floor[startGap-1];
                    temp[i-1][floor[i]] = 1;
                    floor[i-1] = floor[i];
                }
                gapWidth = 0;
            } else 
            {
                if(gapWidth == 0)
                {
                    startGap = i;
                }
                gapWidth += 1;
            }
            // end gap rule
            
            // rule (if next floor is more than 3 tiles up it should be lowered)
            temp = floorWithinJumpRange(i, i+1, temp, 3);
        }
        return temp;
        
    }
    
    private int[][] floorWithinJumpRange(int firstTile, int secondTile, int[][]temp, int maxDifference)
    {
        if(firstTile >= 0 && secondTile < width - 1 && floor[firstTile] != -1 && floor[secondTile] != -1 && (floor[firstTile] - floor[secondTile]) > maxDifference)
        {
            int newFloorHeight = floor[firstTile] - maxDifference;
            temp[secondTile][floor[secondTile]] = 0;
            floor[secondTile] = newFloorHeight;
            temp[secondTile][newFloorHeight] = 1;
        }
        
        return temp;
    }
    
    private void removeDoubleBlocks()
    {
        int[][]temp = land;
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                if(land[i][j] == 1 && j > 0 && j < height - 1 && land[i][j+1] == 1)
                {
                    temp[i][j] = 0;
                }
            }
        }
        land = temp;
    }
    
    private void generatePlatforms()
    {
        int startY = -1;
        int count = 0;
        
        for(int i = 20; i < width - 20; i++)
        {
            int j = 0;
            while(j<height-1 && land[i][j] != 1)
            {
                j++;
            }
            
            if(land[i][j] == 1 && land[i-1][j] == 1 && land[i+1][j] == 1)
            {
                if(startY == -1 || startY != j)
                {
                    startY = j;
                    count = 0;
                }
                
                count ++;
            }else
            {
                if(count >= 3)
                {
                    while(count > 0)
                    {
                        if(startY - 4 > 0 && land[i - count - 1][startY - 4] != 1 && land[i + 1][startY - 4] != 1)
                        {
                            land[i - count][startY - 4] = 4;
                        }
                        count --;
                    }
                }
                
                startY = -1;
                count = 0;
            }
        }
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
    
    private void makeBreakablePlatforms()
    {
        int[][] nextTo = nextToLand(land, 4);
        
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                if(land[i][j] == 4 && i > 1 && i < width - 1 && land[i-1][j] == 5)
                {
                    land[i][j] = 5;
                }else if(land[i][j] == 4 && i > 1 && i < width - 1 && land[i][j] == 4 && nextTo[i][j] == 2 && nextTo[i-1][j] == 2 && nextTo[i+1][j] == 2)
                {
                    land[i-2][j] = 5;
                    land[i-1][j] = 5;
                    land[i][j] = 5;
                    land[i+1][j] = 5;
                    land[i+2][j] = 5;
                }
            }
        }
    }
    
    private void placeMissingWalls()
    {
        for(int i = 0; i < width; i++)
        {
            for(int j = 0; j < height; j++)
            {
                if(j < height - 1 && (land[i][j] == 1 || land[i][j] == 3) && (land[i][j+1] != 1 && land[i][j+1] != 3))
                {
                    setWall(i, j);
                }
            }
        }
    }
    
    /*
     * Helper Function check for only air below a block
     */
    private boolean checkNoSpecificTileBelow(int i, int j, int tileID)
    {
        boolean result = true;
        for(int x = (j+1); x < height; x++)
        {
            if(land[i][x] == tileID)
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
        for(int y = (j+1); y < height; y++)
        {
            if(land[i][y] != 1)
            {
                land[i][y] = 3;
            }
        }
    }

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