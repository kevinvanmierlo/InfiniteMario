/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mojang.mario.level;

import java.util.Random;

/**
 *
 * @author kevinvanmierlo
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
//        placeCoins();
//        landCleanUp();
//        createWalls();
//        makePlatforms();
//        makeBreakablePlatforms();
//        placeMissingWalls();
//        makeFloor();
        
        removeDoubleBlocks();
        generatePlatforms();
        makeBreakablePlatforms();
        placeMissingWalls();
    }
    
    public int[][] getLand()
    {
        return land;
    }
    
    public void processN(int n) 
    {
        //*
        for(int i = 0;i < n; i++){
                //land = process(land);
            //generateLand(); 
            getFloor();
            land = updateLand(land);
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
//
//    
//    /*
//     * code to make the first 20 blocks floor
//     */
//    private void makeFloor(){
//        int count = 0;
//        int floorHeight = 0;
//        
//        for(int i = 0; (i < land.length && i < 20); i++){
//            for(int j = 0; j < land[0].length; j++){
//                if(land[i][j] == 1){
//                    floorHeight += i;
//                    count++;
//                }
//            }
//        }
//        
//        floorHeight = floorHeight/count + 5;
//        if(floorHeight > (land[0].length - 1)){
//            floorHeight = land[0].length - 2;
//        }
//        
//        for(int i = 0; (i < land.length && i < 20); i++){
//            for(int j = 0; j < land[0].length; j++){
//                if(j > floorHeight){
//                    land[i][j] = 3;
//                } else if(j == floorHeight){
//                    land[i][j] = 1;
//                } else {
//                    land[i][j] = 0;
//                }
//            }
//        }
//    }
//    
//    public void placeCoins() {
//        int[][] topBottom = betweenLand(land, 1);
//        for(int i = 0; i < land.length - 1; i++){
//            for(int j = 0; j < land[0].length -1; j++){
//                if(land[i][j] == 1 && topBottom[i][j] == 0){
//                    if(j < land[0].length - 2 && land[i][j+2] == 1){
//                         land[i][j] = 2;
//                    }
//                }
//            }
//        }
//    }
//    
//        private void makePlatforms()
//    {
//        int[][] topBottom = betweenLand(land, 3);
//        int[][] topBottomFloor = betweenLand(land, 1);
//        for(int i = 0; i < width-1; i++)
//        {
//            for(int j = 0; j < height - 3; j++)
//            {
//                if(topBottom[i][j] == 0 && topBottomFloor[i][j] == 0 && land[i][j] == 1)
//                {
//                    if(i > 0 && land[i-1][j] == 1)
//                    {
////                        land[i][j] = 1;
//                    }else if(i < width - 1 && !(topBottom[i+1][j] == 0 && topBottomFloor[i+1][j] == 0) && land[i+1][j] == 1)
//                    {
//                        int x = i-1;
//                        while(x>=0 && land[x][j] == 4)
//                        {
//                            land[x][j] = 1;
//                            x--;
//                        }
//                    }
//                    else
//                    {
//                        land[i][j] = 4;
//                    }
//                }
//            }
//        }
//    }
//    
//    private void makeBreakablePlatforms()
//    {
//        int[][] nextTo = nextToLand(land, 4);
//        
//        for(int i = 0; i < width; i++)
//        {
//            for(int j = 0; j < height; j++)
//            {
//                if(land[i][j] == 4 && i > 1 && i < width - 1 && land[i-1][j] == 5)
//                {
//                    land[i][j] = 5;
//                }else if(land[i][j] == 4 && i > 1 && i < width - 1 && land[i][j] == 4 && nextTo[i][j] == 2 && nextTo[i-1][j] == 2 && nextTo[i+1][j] == 2)
//                {
//                    land[i-2][j] = 5;
//                    land[i-1][j] = 5;
//                    land[i][j] = 5;
//                    land[i+1][j] = 5;
//                    land[i+2][j] = 5;
//                }
//            }
//        }
//    }
//    
//    private int[][] nextToLand(int[][] land, int tile){
//        int[][] temp = new int[width][height];
//        for(int i = 0; i < width; i++){
//            for(int j = 0; j < height; j++){
//                temp[i][j] = 0;
//                if(i>0){
//                    temp[i][j] += (land[i-1][j] == tile) ? 1 : 0; // left
//                                    }
//                if(i < width - 1){
//                    temp[i][j] += (land[i+1][j] == tile) ? 1 : 0; // right
//                }
//            }
//        }
//        return temp;
//    }
//    
//    private int[][] betweenLand(int[][] land, int tile){
//        int[][] temp = new int[width][height];
//        for(int i = 0; i < width; i++){
//            for(int j = 0; j < height; j++){
//                temp[i][j] = 0;
//                if(j>0){
//                    temp[i][j] += (land[i][j-1] == tile) ? 1 : 0; // top
//                }
//                if(j < height - 1){
//                    temp[i][j] += (land[i][j+1] == tile) ? 1 : 0; // bottom
//                }
//            }
//        }
//        return temp;
//    }
//    
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
//    
//    private int[][] updateLand(int[][] land){
//        //int[][] neighboors = neighboorsLand(land);
//        int[][] nextTo = nextToLand(land, 1);
//        int[][] topBottom = betweenLand(land, 1);
//        int[][] temp = land;
//        for(int i = 0; i < width; i++)
//        {
//            for(int j = 0; j < height; j++)
//            {
//                //rule one (no neighboors become air)
//                if(nextTo[i][j] == 0)
//                {
//                    temp[i][j] = 0;
//                } else if(nextTo[i][j] == 2)
//                { //rule two (block needs to be at least 3)
//                        temp[i][j] = 1;
//                } else 
//                {
//                    //*
//                    if(land[i][j] == 1)
//                    {
//                        if(nextTo[i][j] == 1)
//                        {
//                            if(i > 0 && nextTo[i-1][j] == 2)
//                            {
//                                temp[i][j] = 1;
//                            } else if(i < width-1 && nextTo[i+1][j] == 2)
//                            {
//                                temp[i][j] = 1;
//                            } else 
//                            {
//                                temp[i][j] = 0;
//                            }
//                        }
//                    }
//                    //*/
//                }
//                
//                // rule three (when a bock has no block above and below look at block above it that one has two topbottom neighboors.
//                // if it has remove the origional block)
//                if(land[i][j] == 1 && topBottom[i][j] == 0)
//                {
//                    if(j > 0 && topBottom[i][j-1] == 2)
//                    {
//                         temp[i][j] = 0;
//                    }
//                }
//                
//                // rule five (if air has 1 neighboor below with a solid neighboor then remove this first neighboor below the air)
//                if(land[i][j] == 0 && topBottom[i][j] == 1)
//                {
//                    if(j > height-1 && land[i][j+1] == 1 && topBottom[i][j+1] == 1)
//                    {
//                        temp[i][j+1] = 0;
//                    }
//                }
//                
//                if(j <= 3)
//                {
//                    temp[i][j] = 0;
//                }
//            }
//        }
//        return temp;
//        
//    }
//    
//    private int[][] landCleanUp(){
//        int[][] temp = land;
//        int[][] topBottom = betweenLand(land,1);
//        int[][] neighboors = nextToLand(land,1);
//        
//        for(int i = 0; i < width; i++){
//            for(int j = 0; j < height; j++){
//                //*
//                // rule foor (when air is enclosed in blocks fill the air)
//                if(land[i][j] == 0 && topBottom[i][j] == 2 ){//&& ((i > 0 && topBottom[i-1][j] == 1) || (i < (land.length - 2) && topBottom[i+1][j] == 1))){
//                    temp[i][j-1] = 1;
//                    temp[i][j] = 1;
//                    temp[i][j+1] = 1;
//                }
//                //*/
//                
//                //*
//                if(land[i][j] == 1 && neighboors[i][j] == 0){
//                    temp[i][j] = 0;
//                }
//                //*/
//                
//                //*
//                // place wall below platform if platform is 2 thick
//                if(land[i][j] == 1 && (topBottom[i][j] > 0 || land[i][j] == 3)){
//                    if(j > 0 && (land[i][j-1] == 3 || land[i][j-1] == 1)){
//                        land[i][j] = 3;
//                    }
//                }
//                if(land[i][j] == 0 && j > 0 && land[i][j-1] == 3){
//                    land[i][j] = 3;
//                }
//                //*/
//            }
//        }
//        
//        return temp;
//    }
//    
//    private void createWalls(){
//        for(int i = 0; i < land.length; i++){
//            for(int j = 0; j < land[0].length; j++){
//                //*
//                // place wall below platform in bottom x blocks of level if the floor has only air below
//                if(j > (land[0].length - 10) && land[i][j] == 1){
//                    if(checkAir(i,j)){
//                        setWall(i,j);
//                    }
//                }
//                //*/
//            }
//        }         
//    }
//    
//    /*
//     * Create the walls under the ground platforms that are still floating in the air
//     * also make sure the walls continue to the bottom of the screen
//     */
//    private void placeMissingWalls()
//    {
//        for(int i = 0; i < width; i++)
//        {
//            for(int j = 0; j < height; j++)
//            {
//                if(j < height - 1 && (land[i][j] == 1 || land[i][j] == 3) && (land[i][j+1] != 1 && land[i][j+1] != 3))
//                {
//                    setWall(i, j);
//                }
//            }
//        }
//    }
//    
//    /*
//     * Helper Function check for only air below a block
//     */
//    private boolean checkAir(int i, int j){
//        boolean result = true;
//        for(int x = (j+1); x < land[0].length; x++){
//            if(land[i][x] == 1){
//                result = false;
//            }
//        }
//        
//        return result;
//    }
//    
//    /*
//     * Helper Function place wall below block till botom of world
//     */
//    private void setWall(int i, int j){
//        for(int x = (j+1); x < land[0].length; x++){
//            land[i][x] = 3;
//        }
//    }
//    
    private void generateLand(){
        if(land != null){
            for(int i = 0; i < width; i++){
                for(int j = 0; j < height; j++){
                    land[i][j] = ((Math.random() + ((double)j/(height*1.7))) > 0.90 ? 1 : 0);
                }
            }
        }
    }
}
