import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static java.lang.IO.println;

void main() {
    SwingUtilities.invokeLater(() -> new FlappyBirdGame().start());
}

static class FlappyBirdGame {
    private GameFrame frame;
    
    void start() {
        this.frame = new GameFrame();
        this.frame.setVisible(true);
    }
    
    // 游戏主窗口
    static class GameFrame extends JFrame {
        GameFrame() {
            this.setTitle("Flappy Bird");
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setResizable(false);
            this.add(new GamePanel());
            this.pack();
            this.setLocationRelativeTo(null);
        }
    }
    
    // 游戏面板
    static class GamePanel extends JPanel implements ActionListener, KeyListener {
        private static final int WIDTH = 800;
        private static final int HEIGHT = 600;
        private static final int BIRD_SIZE = 30;
        private static final int PIPE_WIDTH = 80;
        private static final int PIPE_GAP = 200;
        private static final int GRAVITY = 1;
        private static final int JUMP_STRENGTH = -15;
        private static final int PIPE_SPEED = 5;
        
        private Bird bird;
        private List<Pipe> pipes;
        private Timer timer;
        private boolean gameOver;
        private boolean started;
        private int score;
        private Random random;
        
        GamePanel() {
            this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
            this.setBackground(new Color(135, 206, 235)); // 天空蓝
            this.setFocusable(true);
            this.addKeyListener(this);
            
            this.random = new Random();
            this.initGame();
        }
        
        private void initGame() {
            this.bird = new Bird(WIDTH / 4, HEIGHT / 2);
            this.pipes = new ArrayList<>();
            this.gameOver = false;
            this.started = false;
            this.score = 0;
            
            // 初始化管道
            for (int i = 0; i < 3; i++) {
                this.addPipe(WIDTH + i * 400);
            }
            
            // 创建游戏定时器
            this.timer = new Timer(20, this);
            this.timer.start();
        }
        
        private void addPipe(int x) {
            int minHeight = 100;
            int maxHeight = HEIGHT - PIPE_GAP - minHeight - 100;
            int topHeight = minHeight + this.random.nextInt(maxHeight);
            this.pipes.add(new Pipe(x, topHeight));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!this.gameOver && this.started) {
                // 更新小鸟
                this.bird.update();
                
                // 更新管道
                for (var pipe : this.pipes) {
                    pipe.update();
                    
                    // 检查碰撞
                    if (this.checkCollision(pipe)) {
                        this.gameOver = true;
                        println("游戏结束！最终得分: " + this.score);
                    }
                    
                    // 计分
                    if (!pipe.passed && pipe.x + PIPE_WIDTH < this.bird.x) {
                        pipe.passed = true;
                        this.score++;
                    }
                }
                
                // 移除屏幕外的管道并添加新管道
                this.pipes.removeIf(p -> p.x + PIPE_WIDTH < 0);
                if (!this.pipes.isEmpty()) {
                    var lastPipe = this.pipes.get(this.pipes.size() - 1);
                    if (lastPipe.x < WIDTH - 400) {
                        this.addPipe(WIDTH);
                    }
                }
                
                // 检查小鸟是否出界
                if (this.bird.y < 0 || this.bird.y > HEIGHT) {
                    this.gameOver = true;
                    println("游戏结束！最终得分: " + this.score);
                }
            }
            
            this.repaint();
        }
        
        private boolean checkCollision(Pipe pipe) {
            // 检查小鸟是否与管道碰撞
            if (this.bird.x + BIRD_SIZE > pipe.x && this.bird.x < pipe.x + PIPE_WIDTH) {
                if (this.bird.y < pipe.topHeight || this.bird.y + BIRD_SIZE > pipe.topHeight + PIPE_GAP) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            var g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 绘制管道
            g2d.setColor(new Color(34, 139, 34)); // 绿色
            for (var pipe : this.pipes) {
                // 上管道
                g2d.fillRect(pipe.x, 0, PIPE_WIDTH, pipe.topHeight);
                // 下管道
                g2d.fillRect(pipe.x, pipe.topHeight + PIPE_GAP, PIPE_WIDTH, HEIGHT - pipe.topHeight - PIPE_GAP);
                
                // 管道边框
                g2d.setColor(new Color(0, 100, 0));
                g2d.drawRect(pipe.x, 0, PIPE_WIDTH, pipe.topHeight);
                g2d.drawRect(pipe.x, pipe.topHeight + PIPE_GAP, PIPE_WIDTH, HEIGHT - pipe.topHeight - PIPE_GAP);
                g2d.setColor(new Color(34, 139, 34));
            }
            
            // 绘制小鸟
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(this.bird.x, this.bird.y, BIRD_SIZE, BIRD_SIZE);
            g2d.setColor(Color.ORANGE);
            g2d.drawOval(this.bird.x, this.bird.y, BIRD_SIZE, BIRD_SIZE);
            
            // 绘制眼睛
            g2d.setColor(Color.BLACK);
            g2d.fillOval(this.bird.x + 20, this.bird.y + 8, 5, 5);
            
            // 绘制分数
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.drawString(String.valueOf(this.score), WIDTH / 2 - 20, 50);
            
            // 绘制提示信息
            if (!this.started) {
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                var message = "按空格键开始游戏";
                var fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(message);
                g2d.drawString(message, (WIDTH - textWidth) / 2, HEIGHT / 2 - 50);
            }
            
            if (this.gameOver) {
                g2d.setColor(new Color(255, 0, 0, 180));
                g2d.fillRect(0, 0, WIDTH, HEIGHT);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 48));
                var gameOverText = "游戏结束！";
                var fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(gameOverText);
                g2d.drawString(gameOverText, (WIDTH - textWidth) / 2, HEIGHT / 2 - 50);
                
                g2d.setFont(new Font("Arial", Font.BOLD, 32));
                var scoreText = "得分: " + this.score;
                textWidth = g2d.getFontMetrics().stringWidth(scoreText);
                g2d.drawString(scoreText, (WIDTH - textWidth) / 2, HEIGHT / 2 + 10);
                
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                var restartText = "按 R 重新开始";
                textWidth = g2d.getFontMetrics().stringWidth(restartText);
                g2d.drawString(restartText, (WIDTH - textWidth) / 2, HEIGHT / 2 + 60);
            }
        }
        
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            
            if (key == KeyEvent.VK_SPACE) {
                if (!this.started) {
                    this.started = true;
                }
                if (!this.gameOver) {
                    this.bird.jump();
                }
            }
            
            if (key == KeyEvent.VK_R && this.gameOver) {
                this.timer.stop();
                this.initGame();
            }
        }
        
        @Override
        public void keyReleased(KeyEvent e) {}
        
        @Override
        public void keyTyped(KeyEvent e) {}
    }
    
    // 小鸟类
    static class Bird {
        private int x;
        private int y;
        private int velocity;
        
        Bird(int x, int y) {
            this.x = x;
            this.y = y;
            this.velocity = 0;
        }
        
        void update() {
            this.velocity += GamePanel.GRAVITY;
            this.y += this.velocity;
        }
        
        void jump() {
            this.velocity = GamePanel.JUMP_STRENGTH;
        }
    }
    
    // 管道类
    static class Pipe {
        private int x;
        private int topHeight;
        private boolean passed;
        
        Pipe(int x, int topHeight) {
            this.x = x;
            this.topHeight = topHeight;
            this.passed = false;
        }
        
        void update() {
            this.x -= GamePanel.PIPE_SPEED;
        }
    }
}
