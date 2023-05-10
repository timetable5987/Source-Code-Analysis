package org.newdawn.spaceinvaders;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.newdawn.spaceinvaders.entity.AlienEntity;
import org.newdawn.spaceinvaders.entity.Entity;
import org.newdawn.spaceinvaders.entity.ShipEntity;
import org.newdawn.spaceinvaders.entity.ShotEntity;

/**
 2인용 모드 클래스입니다.
 사용자1은 A,D KEY로 이동하고 S KEY로 공격합니다.
 사용자2는 좌우 화살표 KEY로 이동하고 아래 화살표 KEY로 공격합니다.
 */
public class TwoPlayer extends Game
{

    private ArrayList entities1 = new ArrayList();
    /** The list of entities that need to be removed from the game this loop */

    private ArrayList entities2 = new ArrayList();

    /** The entity representing the player */
    private Entity ship1;
    private Entity ship2;
    /** The speed at which the player's ship should move (pixels/sec) */

    private boolean logicRequiredThisLoop1 = false;
    private boolean logicRequiredThisLoop2 = false;
    private long lastFire1 = 0;
    private long lastFire2 = 0;
    /** The interval between our players shot (ms) */

    private int alienCount1;
    private int alienCount2;

    /** The message to display which waiting for a key press */

    private boolean leftPressed = false;
    /** True if the right cursor key is currently pressed */
    private boolean leftPressed2 = false;

    private boolean rightPressed = false;
    /** True if we are firing */
    private boolean rightPressed2 = false;
    private boolean firePressed1 = false;
    /** True if game logic needs to be applied this loop, normally as a result of a game event */

    private boolean firePressed2 = false;

    protected long firingInterval = 300;


    /**
     * Construct our game and set it running.
     */
    public TwoPlayer() {
        // create a frame to contain our game
        container = new JFrame("Space Invaders 102");

        // get hold the content of the frame and set up the resolution of the game
        JPanel panel = (JPanel) container.getContentPane();
        panel.setPreferredSize(new Dimension(800,600));
        panel.setLayout(null);

        // setup our canvas size and put it into the content of the frame
        setBounds(0,0,800,600);
        panel.add(this);

        // Tell AWT not to bother repainting our canvas since we're
        // going to do that our self in accelerated mode
        setIgnoreRepaint(true);

        // finally make the window visible
        container.pack();
        container.setResizable(false);
        container.setVisible(true);

        // add a listener to respond to the user closing the window. If they
        // do we'd like to exit the game
        container.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // add a key input system (defined below) to our canvas
        // so we can respond to key pressed
        addKeyListener(new KeyInputHandlerTwo());

        // request the focus so key events come to us
        requestFocus();

        // create the buffering strategy which will allow AWT
        // to manage our accelerated graphics
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        // initialise the entities in our game so there's something
        // to see at startup
        initEntities();
    }

    /**
     * Start a fresh game, this should clear out any old data and
     * create a new set.
     */
    private void startGame() {
        // clear out any existing entities and intialise a new set
        entities1.clear();
        entities2.clear();
        initEntities();

        // blank out any keyboard settings we might currently have
        leftPressed = false;
        leftPressed2 = false;
        rightPressed = false;
        rightPressed2 = false;
        firePressed1 = false;
        firePressed2 = false;
    }

    /**
     * Initialise the starting state of the entities (ship and aliens). Each
     * entitiy will be added to the overall list of entities in the game.
     */
    private void initEntities() {
        // create the player ship and place it roughly in the center of the screen
        ship1 = new ShipEntity(this,"sprites/ship.gif",188,550);
        entities1.add(ship1);

        ship2 = new ShipEntity(this, "sprites/ship2.gif",563,550);
        entities2.add(ship2);

        // create a block of aliens (5 rows, by 12 aliens, spaced evenly)
        alienCount1 = 0;
        alienCount2 = 0;
        for (int row=0;row<5;row++) {
            for (int x=0;x<6;x++) {
                Entity alien = new AlienEntity(this,50+(x*50),(50)+row*30);
                entities1.add(alien);
                alienCount1++;
            }
        }

        for (int row=0;row<5;row++) {
            for (int x=0;x<6;x++) {
                Entity alien2 = new AlienEntity(this,450+(x*50),(50)+row*30);
                entities2.add(alien2);
                alienCount2++;
            }
        }
    }

    /**
     * updateLogic() 메소드를 두 개 사용
     */
    public void updateLogic1() {
        logicRequiredThisLoop1 = true;
    }
    public void updateLogic2() {
        logicRequiredThisLoop2 = true;
    }

    /**
     * Remove an entity from the game. The entity removed will
     * no longer move or be drawn.
     *
     * @param entity The entity that should be removed
     */
    public void removeEntity(Entity entity) {
        removeList.add(entity);
    }

    /**
     * Notification that the player has died.
     */
    public void notifyDeath() {
        message = "Oh no! They got you, try again?";
        waitingForKeyPress = true;
    }

    /**
     *어떤 사용자가 이겼는지 알려줌
     */
    public void notifyWin1() {
        message = "Well done! User1 Win!";
        waitingForKeyPress = true;
    }

    public void notifyWin2() {
        message = "Well done! User2 Win!";
        waitingForKeyPress = true;
    }

    /**
     * 어떤 사용자가 죽었는지 알려줌
     */
    public void notifyAlienKilled1() {
        // reduce the alient count, if there are none left, the player has won!
        alienCount1--;
        gameSound.soundPlay("bgm/monster.wav",false);
        //몬스터 처치 효과음 추가

        if (alienCount1 == 0) { notifyWin1();}

        // if there are still some aliens left then they all need to get faster, so
        // speed up all the existing aliens
        for (int i=0;i<entities1.size();i++) {
            Entity entity1 = (Entity) entities1.get(i);
            if (entity1 instanceof AlienEntity) {
                // speed up by 2%
                entity1.setHorizontalMovement(entity1.getHorizontalMovement() * 1.02);
            }
        }

    }
    public void notifyAlienKilled2() {
        // reduce the alient count, if there are none left, the player has won!
        alienCount2--;
        gameSound.soundPlay("bgm/monster.wav",false);
        //몬스터 처치 효과음 추가

        if (alienCount2 == 0) { notifyWin2();}


        // if there are still some aliens left then they all need to get faster, so
        // speed up all the existing aliens


        for (int j=0;j<entities2.size();j++) {
            Entity entity2 = (Entity) entities2.get(j);

            if (entity2 instanceof AlienEntity) {
                // speed up by 2%
                entity2.setHorizontalMovement(entity2.getHorizontalMovement() * 1.02);
            }
        }
    }

    /**
     * 공격 메소드를 두 개 작성
     */
    public void tryToFire1() {
        // check that we have waiting long enough to fire
        if (System.currentTimeMillis() - lastFire1 < firingInterval) {
            return;
        }

        // if we waited long enough, create the shot entity, and record the time.
        lastFire1 = System.currentTimeMillis();
        ShotEntity shot1 = new ShotEntity(this,"sprites/shot.gif",ship1.getX()+10,ship1.getY()-30);
        entities1.add(shot1);
        gameSound.soundPlay("bgm/fire.wav",false);


    }
    public void tryToFire2() {
        // check that we have waiting long enough to fire
        if (System.currentTimeMillis() - lastFire2 < firingInterval) {
            return;
        }

        // if we waited long enough, create the shot entity, and record the time.
        lastFire2 = System.currentTimeMillis();

        ShotEntity shot2 = new ShotEntity(this,"sprites/shot.gif",ship2.getX()+10,ship2.getY()-30);
        entities2.add(shot2);
        gameSound.soundPlay("bgm/fire.wav",false);


    }




    /**
     * The main game loop. This loop is running during all game
     * play as is responsible for the following activities:
     * <p>
     * - Working out the speed of the game loop to update moves
     * - Moving the game entities
     * - Drawing the screen contents (entities, text)
     * - Updating game events
     * - Checking Input
     * <p>
     */
    public void gameLoop() {
        long lastLoopTime = SystemTimer.getTime();

        gameSound.soundPlay("bgm/bg.wav", true);
        // 배경음 추가

        // keep looping round til the game ends
        while (gameRunning) {
            // work out how long its been since the last update, this
            // will be used to calculate how far the entities should
            // move this loop
            long delta = SystemTimer.getTime() - lastLoopTime;
            lastLoopTime = SystemTimer.getTime();

            // update the frame counter
            lastFpsTime += delta;
            fps++;

            // update our FPS counter if a second has passed since
            // we last recorded
            if (lastFpsTime >= 1000) {
                container.setTitle(windowTitle+" (FPS: "+fps+")");
                lastFpsTime = 0;
                fps = 0;
            }

            // Get hold of a graphics context for the accelerated
            // surface and blank it out
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            g.setColor(Color.black);
            g.fillRect(0,0,800,600);

            // 객체 움직임을 사용자마다 다르게 정의함
            if (!waitingForKeyPress) {
                for (int i=0;i<entities1.size();i++) {
                    Entity entity1 = (Entity) entities1.get(i);

                    entity1.move1(delta);
                }
            }

            if (!waitingForKeyPress) {
                for (int i=0;i<entities2.size();i++) {
                    Entity entity2 = (Entity) entities2.get(i);

                    entity2.move2(delta);
                }
            }


            // 엔티티 객체도 두 배로 늘림
            for (int i=0;i<entities1.size();i++) {
                Entity entity1 = (Entity) entities1.get(i);

                entity1.draw(g);
            }

            for (int i=0;i<entities2.size();i++) {
                Entity entity2 = (Entity) entities2.get(i);

                entity2.draw(g);
            }

            // brute force collisions, compare every entity against
            // every other entity. If any of them collide notify
            // both entities that the collision has occured
            for (int p=0;p<entities1.size();p++) {
                for (int s=p+1;s<entities1.size();s++) {
                    Entity me = (Entity) entities1.get(p);
                    Entity him = (Entity) entities1.get(s);

                    if (me.collidesWith1(him)) {
                        me.collidedWith1(him);
                        him.collidedWith1(me);
                    }
                }
            }

            for (int p=0;p<entities2.size();p++) {
                for (int s=p+1;s<entities2.size();s++) {
                    Entity me = (Entity) entities2.get(p);
                    Entity him = (Entity) entities2.get(s);

                    if (me.collidesWith2(him)) {
                        me.collidedWith2(him);
                        him.collidedWith2(me);
                    }
                }
            }

            // remove any entity that has been marked for clear up
            entities1.removeAll(removeList);
            entities2.removeAll(removeList);
            removeList.clear();

            // if a game event has indicated that game logic should
            // be resolved, cycle round every entity requesting that
            // their personal logic should be considered.
            if (logicRequiredThisLoop1) {
                for (int i=0;i<entities1.size();i++) {
                    Entity entity = (Entity) entities1.get(i);
                    entity.doLogic1();
                }


                logicRequiredThisLoop1 = false;
            }

            if (logicRequiredThisLoop2) {
                for (int i=0;i<entities2.size();i++) {
                    Entity entity2 = (Entity) entities2.get(i);
                    entity2.doLogic2();
                }


                logicRequiredThisLoop2 = false;
            }

            // if we're waiting for an "any key" press then draw the
            // current message
            if (waitingForKeyPress) {
                g.setColor(Color.white);
                g.drawString(message,(800-g.getFontMetrics().stringWidth(message))/2,250);
                g.drawString("Press any key",(800-g.getFontMetrics().stringWidth("Press any key"))/2,300);
            }

            // finally, we've completed drawing so clear up the graphics
            // and flip the buffer over
            g.dispose();
            strategy.show();

            // resolve the movement of the ship. First assume the ship
            // isn't moving. If either cursor key is pressed then
            // update the movement appropraitely
            ship1.setHorizontalMovement(0);
            ship2.setHorizontalMovement(0);

            if ((leftPressed) && (!rightPressed)) {
                ship1.setHorizontalMovement(-moveSpeed);
            } else if ((rightPressed) && (!leftPressed)) {
                ship1.setHorizontalMovement(moveSpeed);
            }

            if ((leftPressed2) && (!rightPressed2)) {
                ship2.setHorizontalMovement(-moveSpeed);
            } else if ((rightPressed2) && (!leftPressed2)) {
                ship2.setHorizontalMovement(moveSpeed);
            }

            // if we're pressing fire, attempt to fire

            if (firePressed1) { tryToFire1();}

            if (firePressed2) {tryToFire2();}

            // we want each frame to take 10 milliseconds, to do this
            // we've recorded when we started the frame. We add 10 milliseconds
            // to this and then factor in the current time to give
            // us our final value to wait for
            SystemTimer.sleep(lastLoopTime+10-SystemTimer.getTime());
        }
    }

    /**
     * A class to handle keyboard input from the user. The class
     * handles both dynamic input during game play, i.e. left/right
     * and shoot, and more static type input (i.e. press any key to
     * continue)
     *
     * This has been implemented as an inner class more through
     * habbit then anything else. Its perfectly normal to implement
     * this as seperate class if slight less convienient.
     *
     * @author Kevin Glass
     */
    private class KeyInputHandlerTwo extends KeyAdapter {
        /** The number of key presses we've had while waiting for an "any key" press */
        private int pressCount = 1;

        /**
         * Notification from AWT that a key has been pressed. Note that
         * a key being pressed is equal to being pushed down but *NOT*
         * released. Thats where keyTyped() comes in.
         *
         * @param e The details of the key that was pressed
         */
        public void keyPressed(KeyEvent e) {
            // if we're waiting for an "any key" typed then we don't
            // want to do anything with just a "press"
            if (waitingForKeyPress) {
                return;
            }

            //사용자1은 a,d로 움직이고 s키로 공격
            //사용자2는 방향키로 움직이고 아래키로 공격

            if (e.getKeyCode() == KeyEvent.VK_A) {
                leftPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_D) {
                rightPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_S) {
                firePressed1 = true;
            }

            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed2 = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed2 = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                firePressed2 = true;
            }

        }

        /**
         * Notification from AWT that a key has been released.
         *
         * @param e The details of the key that was released
         */
        public void keyReleased(KeyEvent e) {
            // if we're waiting for an "any key" typed then we don't
            // want to do anything with just a "released"
            if (waitingForKeyPress) {
                return;
            }

            if (e.getKeyCode() == KeyEvent.VK_A) {
                leftPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_D) {
                rightPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_S) {
                firePressed1 = false;
            }

            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed2 = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed2 = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                firePressed2 = false;
            }
        }

        /**
         * Notification from AWT that a key has been typed. Note that
         * typing a key means to both press and then release it.
         *
         * @param e The details of the key that was typed.
         */
        public void keyTyped(KeyEvent e) {
            // if we're waiting for a "any key" type then
            // check if we've recieved any recently. We may
            // have had a keyType() event from the user releasing
            // the shoot or move keys, hence the use of the "pressCount"
            // counter.
            if (waitingForKeyPress) {
                if (pressCount == 1) {
                    // since we've now recieved our key typed
                    // event we can mark it as such and start
                    // our new game
                    waitingForKeyPress = false;
                    startGame();
                    pressCount = 0;
                } else {
                    pressCount++;
                }
            }

            // if we hit escape, then quit the game
            if (e.getKeyChar() == 27) {
                System.exit(0);
            }
        }
    }



    /**
     * The entry point into the game. We'll simply create an
     * instance of class which will start the display and game
     * loop.
     *
     * @param argv The arguments that are passed into our game
     */
    public static void main(String argv[]) {
        TwoPlayer t = new TwoPlayer();

        // Start the main game loop, note: this method will not
        // return until the game has finished running. Hence we are
        // using the actual main thread to run the game.
        t.gameLoop();
    }
}
