/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mojang.mario.level;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

    /*
    0  = air
    1  = land
    2  = coins
    3  = wall
    4  = question block
    5  = breakable block
    
    10 = floor left edge
    11 = floor right edge
    12 = mountain left edge
    13 = mountain right edge
    14 = floor left wall
    15 = floor right wall
    16 = mountain left wall
    17 = mountain right wall
    */

/**
 *
 * @author Thomas & Kevin
 */
public class CellularAutomataLevelGenerator 
{
    private int[][] land;
    private int[] floor;
    private int width;
    private int height;
    
    public int beginFloorHeight = 0;
    
    public CellularAutomataLevelGenerator(int width, int height)
    {
        land = new int[width][height];
        this.floor = new int[width];
        this.width = width;
        this.height = height;
        generateLand();
        processN(10);
        
        removeDoubleBlocks();
        generatePlatforms();
        makeBreakablePlatforms();
        placeMissingWalls();
        fixWalls();
        
        // Uncomment this to get the level in our own JFrame
//        new CellularAutomata();
    }
    
    class CellularAutomata extends JFrame
    {
        private final String OS;    
        private final JPanel worldPanel;
        private int blockSize = 10;
        
        public CellularAutomata()
        {
            OS = System.getProperty("os.name").toLowerCase();

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
                                    case 10:
                                        g.setColor(Color.green);
                                    break;
                                    case 11:
                                        g.setColor(Color.green);
                                    break;
                                    case 12:
                                        g.setColor(Color.blue);
                                    break;
                                    case 13:
                                        g.setColor(Color.blue);
                                    break;
                                    case 14:
                                        g.setColor(Color.CYAN);
                                    break;
                                    case 15:
                                        g.setColor(Color.CYAN);
                                    break;
                                    case 16:
                                        g.setColor(Color.CYAN);
                                    break;
                                    case 17:
                                        g.setColor(Color.CYAN);
                                    break;
                                }

                                g.fillRect(blockSize*i, blockSize*j, blockSize, blockSize);
                            }
                        }
                    }
                }
            };

            // Correct size on windows and mac
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
        }
    }
    
    /**
     * Get the map containing all data
     * @return the map containing all data
     */
    public int[][] getLand()
    {
        return land;
    }
    
    /**
     * Update the land according to our rules
     * @param n number of updates
     */    
    public void processN(int n) 
    {
        for(int i = 0;i < n; i++)
        {
            // First get the floor
            getFloor();
            
            // Then update the land according to our rules
            land = updateLand(land);
        }
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
                }
                
                // rule three (when a block has no block above and no block below, look at the block above it. 
                // If it has a block above and a block below. Remove this block (Bigger gaps (in height) between platforms)
                if(land[i][j] == 1 && topBottom[i][j] == 0)
                {
                    if(j > 0 && topBottom[i][j-1] == 2)
                    {
                         temp[i][j] = 0;
                    }
                }
                
                // rule four (two tiles above floor becomes air)
                if(floor[i] - 2 == j || floor[i] - 1 == j)
                {
                    temp[i][j] = 0;
                }
                
                // rule five (top 4 tiles become air)
                if(j <= 3)
                {
                    temp[i][j] = 0;
                }
                
                // rule six (first and last 20 tiles become straight floor)
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
            
            // rule seven (gaps should not be too wide)
            if(floor[i] != -1 && i < (width - 1))
            {
                // sub rule (max height between floors are less when theres a gap between)
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
            
            // rule eight (if next floor is more than 3 tiles up it should be lowered)
            temp = floorWithinJumpRange(i, i+1, temp, 3);
        }
        return temp;
        
    }
    
    /**
     * Check if secondTile is between maxDifference, if not secondTile is lowered
     * @param firstTile the first tile on the x-axis (used to check difference with secondTile)
     * @param secondTile the second tile on the x-axis (used to check difference with firstTile)
     * @param temp the temp land for updating tiles
     * @param maxDifference the maximum difference between the two tiles
     * @return 
     */
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
    
    /**
     * Remove this block if there is another block below
     */
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
    
    /**
     * Generate question blocks. On top of floor or mountain if the size is bigger than 2 blocks.
     * Doesn't appear above edges of floor or mountain.
     */
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
    
    /**
     * Used to get the neighbours on the right and left
     * @param land The map to get the neighbours from
     * @param tile The kind of tile you want to check for neighbours
     * @return the map with numbers as how many neighbours (land[i][j] = 1, so has 1 neighbour)
     */
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
    
    /**
     * Used to get the neighbours above and below
     * @param land The map to get the neighbours from
     * @param tile The kind of tile you want to check for neighbours
     * @return the map with numbers as how many neighbours (land[i][j] = 1, so has 1 neighbour)
     */
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
    
    /**
     * If it is a question block which is more than 4 wide, it becomes a breakable platform
     */
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
    
    /**
     * Put a wall below every land
     */
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
    
    /**
     * This fixes the walls and puts edges on the platforms and floor
     */
    private void fixWalls()
    {
        int[][] temp = copyArray(land);
        
        for(int i = 19; i < width - 18; i++)
        {
            for(int j = 0; j < height; j++)
            {
                if(land[i][j] == 1)
                {
                    if(land[i-1][j] != 1)
                    {
                        if(floor[i] == j)
                        {
                            temp[i][j] = 10;
                            int y = j + 1;
                            while(y < height && temp[i][y] == 3)
                            {
                                temp[i][y] = 14;
                                y++;
                            }
                        }else
                        {
                            temp[i][j] = 12;
                            int y = j + 1;
                            while(y < height && temp[i][y] == 3)
                            {
                                temp[i][y] = 16;
                                y++;
                            }
                        }
                    }else if(land[i+1][j] != 1)
                    {
                        if(floor[i] == j)
                        {
                            temp[i][j] = 11;
                            int y = j + 1;
                            while(y < height && temp[i][y] == 3)
                            {
                                temp[i][y] = 15;
                                y++;
                            }
                        }else
                        {
                            temp[i][j] = 13;
                            int y = j + 1;
                            while(y < height && temp[i][y] == 3)
                            {
                                temp[i][y] = 17;
                                y++;
                            }
                        }
                    }
                }
            }
        }
        land = temp;
    }
    
    /**
     * Helper function to check if there is no tile below with tileID
     * @param i the parameter on the x-axis
     * @param j the parameter on the y-axis
     * @param tileID the ID of the tile you want to check
     * @return 
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
    
    /**
     * Helper function to place a wall below a block untill you reach the bottom of the world
     * @param i the parameter on the x-axis
     * @param j the parameter on the y-axis
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

    /**
     * Generate all the blocks in the beginning (seed)
     */
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
    
    /**
     * Copy the content of an array and not the reference
     * @param original original array to copy the content from
     * @return a new array with the same content as the original
     */
    public int[][] copyArray(int[][] original) 
    {
        if (original == null) 
        {
            return null;
        }

        final int[][] result = new int[original.length][];
        for (int i = 0; i < original.length; i++) 
        {
            result[i] = Arrays.copyOf(original[i], original[i].length);
        }
        return result;
    }
}
