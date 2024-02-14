import mysql.connector
import json
import re

def verificarString(movimento):
    regex = r'\d+\.'
    return re.search(regex, movimento)

def transformarMovimentos(movimentos):
    movesArray = movimentos.split();
    return [string for string in movesArray if not verificarString(string)]
    
conection = mysql.connector.connect(
    host="localhost",
    user="aluno",
    password="aluno",
    database="chesszika"
)

cursor = conection.cursor()

with open ('./openings.json') as arquivo:
    dados = json.load(arquivo);
    
query = "INSERT INTO abertura (movimentos_abertura, nome_abertura) VALUES(%s, %s)"

for i, op in enumerate(dados):
    moves = ', '.join(transformarMovimentos(op['moves']))
    name = op['name']
    cursor.execute(query, (moves, name))
    conection.commit()

cursor.close()
conection.close()
