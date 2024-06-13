# BOT-POKEMMO

[PT]
Os códigos foram desenvolvidos inteiramentes em Java com a intenção de serem mais leves e flexíveis para compartilhar com outros jogadores (como se eu tivesse público para isso, rsrs). No entanto, há apenas um .exe gerado a partir de um arquivo Python (precisa utilizar a função `locateOnScreen` da biblioteca `pyautogui` e não encontrei nada semelhante em Java).

Os bots apresentam uma interface gráfica simples (até demais), oferecendo diversos botões dentro de um "Pane" rolável (`JScrollPane`) para que o usuário possa configurar as variáveis de acordo com o seu computador, considerando que cada computador/jogador utiliza uma certa resolução do game, hotkeys, zoom, etc. Confesso que a parte mais complicada no código foi pensar em tudo que varia de computador para computador, portanto, a maior parte do código é configuração de variáveis e verificação de possíveis erros.

## Funcionalidades dos Bots

- **Simulação de Teclas**: O bots contam com um sistema de simulação do pressionamento de teclas (já configuradas pelos usuários) de modo que o personagem no jogo realize ações repetitivas para "farmar". Eles se baseiam na verificação de pixels para saber onde no jogo está atualmente e o que deve ser feito em seguida.

- **OCR (tesseract)**: Utilizado para verificar o nome do Pokémon encontrado.

- **JNativeHook**: Biblioteca para criar um listener que verifica as teclas pressionadas mesmo que o app não esteja em primeiro plano.

- **Threads**: Foi necessário um extenso gerenciamento de threads para que os bots pausassem de maneira correta.

- **Robot**: Para simular a movimentação de mouse e o pressionamento de teclas.

- **LocateOnScreen**: Utilizado apenas no bot de pesca, já que é necessário usar skills diferentes dependendo da situação (e não apenas 1), é utilizado para verificar a localização dos botões na tela (variando a posição para evitar bots). Como só existe algo semelhante em Python, gerei um executável a partir do arquivo "LocateOnScreen.py" com o `pyinstaller` e o executei dentro do Java por meio do `ProcessBuilder`.

## Considerações Finais

Os bots foram criados com a intenção apenas de treinar meus conhecimentos em programação. Portanto, já divulguei e testei os bots com diversos jogadores e, pasme, funcionam muito bem hehe! Já vi até jogadores serem banidos por deixarem farmando a noite toda... Portanto, mesmo sendo um projeto "bobo", fico feliz de ter feito algo funcional que eu sempre tive vontade de fazer.

## Bots Disponíveis

### 1. Bot de Pesca Automática

Este bot permite que você pesque automaticamente, facilitando a coleta de itens valiosos de Pokémons, além de ser uma ótima maneira de ganhar dinheiro dentro do jogo.

<div align="center">
  <img src="https://github.com/RyanMazzeu/BOT-POKEMMO/assets/104333277/5e7d571d-9928-4e77-a807-3c98391c1ba2" alt="Imagem da Interface do Bot de Pesca" width="600">
  <p>Imagem da interface do bot</p>
</div>
<div align="center">
  <img src="https://github.com/RyanMazzeu/BOT-POKEMMO/assets/104333277/cc560540-384c-4c21-ac8e-fb0798fe9545" alt="Gif do Bot de Pesca em Ação" width="600">
  <p>Gif do bot funcionando</p>
</div>

### 2. Bot de Farm de Hordas

Este bot permite que você farme hordas pokemons automaticamente, facilitando o farm de XP e busca por pokemons shiny's.
![image](https://github.com/RyanMazzeu/BOT-POKEMMO/assets/104333277/b2934338-a296-4ad6-bc74-5047c5883db5)
