/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mojang.mario.level;

/**
 *
 * @author kevinvanmierlo
 */
public class CellularAutomataLevelGenerator 
{
    private int[][] land;
    private int width;
    private int height;
    
    public CellularAutomataLevelGenerator(int width, int height)
    {
        land = new int[width][height];
        this.width = width;
        this.height = height;
        generateLand();
        processN(10);
        placeCoins();
        landCleanUp();
        createWalls();
        makeFloor();
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
            land = updateLand(land);
        }
        //*/
    }
    
    /*
     * code to make the first 20 blocks floor
     */
    private void makeFloor(){
        int count = 0;
        int floorHeight = 0;
        
        for(int i = 0; (i < land.length && i < 20); i++){
            for(int j = 0; j < land[0].length; j++){
                if(land[i][j] == 1){
                    floorHeight += i;
                    count++;
                }
            }
        }
        
        floorHeight = floorHeight/count + 5;
        if(floorHeight > (land[0].length - 1)){
            floorHeight = land[0].length - 2;
        }
        
        for(int i = 0; (i < land.length && i < 20); i++){
            for(int j = 0; j < land[0].length; j++){
                if(j > floorHeight){
                    land[i][j] = 3;
                } else if(j == floorHeight){
                    land[i][j] = 1;
                } else {
                    land[i][j] = 0;
                }
            }
        }
    }
    
    public void placeCoins() {
        int[][] topBottom = betweenLand(land, 1);
        for(int i = 0; i < land.length - 1; i++){
            for(int j = 0; j < land[0].length -1; j++){
                if(land[i][j] == 1 && topBottom[i][j] == 0){
                    if(j < land[0].length - 2 && land[i][j+2] == 1){
                         land[i][j] = 2;
                    }
                }
            }
        }
    }
    
    private int[][] nextToLand(int[][] land, int tile){
        int[][] temp = new int[width][height];
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                temp[i][j] = 0;
                if(i>0){
                    temp[i][j] += (land[i-1][j] == tile) ? 1 : 0; // left
                                    }
                if(i < width - 1){
                    temp[i][j] += (land[i+1][j] == tile) ? 1 : 0; // right
                }
            }
        }
        return temp;
    }
    
    private int[][] betweenLand(int[][] land, int tile){
        int[][] temp = new int[width][height];
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                temp[i][j] = 0;
                if(j>0){
                    temp[i][j] += (land[i][j-1] == tile) ? 1 : 0; // top
                }
                if(j < height - 1){
                    temp[i][j] += (land[i][j+1] == tile) ? 1 : 0; // bottom
                }
            }
        }
        return temp;
    }
    
    private int[][] neighboorsLand(int[][] land){
        int[][] temp = new int[width][height];
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                temp[i][j] = 0;
                if(j>0){
                    temp[i][j] += land[i-1][j]; // left
                    if(i>0){
                        temp[i][j] += land[i-1][j-1]; // top-left
                    }
                    if(i<land.length - 1){
                        temp[i][j] += land[i-1][j+1]; // bottom-left
                    }
                }
                if(j < land[0].length - 1){
                    temp[i][j] += land[i+1][j]; // right
                    if(i>0){
                        temp[i][j] += land[i+1][j-1]; // top-right
                    }
                    if(i<land.length - 1){
                        temp[i][j] += land[i+1][j+1]; // bottom-right
                    }
                }
                if(i>0){
                    temp[i][j] += land[i][j-1]; // top
                }
                if(i<land.length - 1){
                    temp[i][j] += land[i][j+1]; // bottom
                }
            }
        }
        return temp;
    }
    
    private int[][] updateLand(int[][] land){
        //int[][] neighboors = neighboorsLand(land);
        int[][] neighboors = nextToLand(land, 1);
        int[][] topBottom = betweenLand(land, 1);
        int[][] temp = land;
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                //rule one (no neighboors become air)
                if(neighboors[i][j] == 0){
                    temp[i][j] = 0;
                } else if(neighboors[i][j] == 2){ //rule two (block needs to be at least 3)
                        temp[i][j] = 1;
                } else {
                    //*
                    if(land[i][j] == 1){
                        if(neighboors[i][j] == 1){
                            if(i > 0 && neighboors[i-1][j] == 2){
                                temp[i][j] = 1;
                            } else if(i < land[0].length-1 && neighboors[i+1][j] == 2){
                                temp[i][j] = 1;
                            } else {
                                temp[i][j] = 0;
                            }
                        }
                    }
                    //*/
                }
                
                // rule tree (when a bock has no block above and below look at block above it that one has two topbottom neighboors.
                // if it has remove the origional block)
                if(land[i][j] == 1 && topBottom[i][j] == 0){
                    if(j > 0 && topBottom[i][j-1] == 2){
                         temp[i][j] = 0;
                    }
                }
                
                // rule five (if air has 1 neighboor below with a solid neighboor then remove this first neighboor below the air)
                if(land[i][j] == 0 && topBottom[i][j] == 1){
                    if(j > land.length-1 && land[i][j+1] == 1 && topBottom[i][j+1] == 1){
                        temp[i][j+1] = 0;
                    }
                }
                
                // rule six (if a hole is equal to or more than 3 wide fill up three spaces)
                // TODO j == height needs to be j == floor
                if(j == height - 1 && land[i][j] == 0 && neighboors[i][j] == 0)
                {
                    temp[i][j] = 1;
                    if(i != width - 1)
                        temp[i+1][j] = 1;
                    else if(i != 0)
                        temp[i-1][j] = 1;
                }
            }
        }
        return temp;
        
    }
    
    private int[][] landCleanUp(){
        int[][] temp = land;
        int[][] topBottom = betweenLand(land,1);
        int[][] neighboors = nextToLand(land,1);
        
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                //*
                // rule foor (when air is enclosed in blocks fill the air)
                if(land[i][j] == 0 && topBottom[i][j] == 2 ){//&& ((i > 0 && topBottom[i-1][j] == 1) || (i < (land.length - 2) && topBottom[i+1][j] == 1))){
                    temp[i][j-1] = 1;
                    temp[i][j] = 1;
                    temp[i][j+1] = 1;
                }
                //*/
                
                //*
                if(land[i][j] == 1 && neighboors[i][j] == 0){
                    temp[i][j] = 0;
                }
                //*/
                
                //*
                // place wall below platform if platform is 2 thick
                if(land[i][j] == 1 && (topBottom[i][j] > 0 || land[i][j] == 3)){
                    if(j > 0 && (land[i][j-1] == 3 || land[i][j-1] == 1)){
                        land[i][j] = 3;
                    }
                }
                if(land[i][j] == 0 && j > 0 && land[i][j-1] == 3){
                    land[i][j] = 3;
                }
                //*/
            }
        }
        
        return temp;
    }
    
    private void createWalls(){
        for(int i = 0; i < land.length; i++){
            for(int j = 0; j < land[0].length; j++){
                //*
                // place wall below platform in bottom x blocks of level if the floor has only air below
                if(j > (land[0].length - 10) && land[i][j] == 1){
                    if(checkAir(i,j)){
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
    private boolean checkAir(int i, int j){
        boolean result = true;
        for(int x = (j+1); x < land[0].length; x++){
            if(land[i][x] == 1){
                result = false;
            }
        }
        
        return result;
    }
    
    /*
     * Helper Function place wall below block till botom of world
     */
    private void setWall(int i, int j){
        for(int x = (j+1); x < land[0].length; x++){
            land[i][x] = 3;
        }
    }
    
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
