#!/usr/bin/env python3

node_count, degree = input("Enter the number of nodes and degree of each node: ").split(" ")
node_count = int(node_count)
degree = int(degree)


assert degree > 1
assert node_count % 2 == 0 or degree % 2 == 0

nbrd = degree // 2

def get_x_steps_back(cur_node, x):
    assert 0 <= cur_node < node_count
    return (cur_node - x) % node_count
    
def get_x_steps_ahead(cur_node, x):
    assert 0 <= cur_node < node_count
    return (cur_node + x) % node_count

def get_opposite(cur_node):
    assert 0 <= cur_node < node_count
    assert node_count % 2 == 0
    return get_x_steps_ahead(cur_node, node_count // 2)

regular_graph = [ [ False for i in range(node_count) ] for j in range(node_count) ]


for i in range(1, nbrd + 1):
    for n in range(len(regular_graph)):
        p = get_x_steps_ahead(n, i)
        q = get_x_steps_back(n, i)

        regular_graph[n][p] = regular_graph[p][n] = True
        regular_graph[n][q] = regular_graph[q][n] = True

        if degree % 2 != 0 and node_count % 2 == 0:
            r = get_opposite(n)
            regular_graph[n][r] = regular_graph[r][n] = True



for n in range(len(regular_graph)):
    row = []
    for i in range(n+1, len(regular_graph)):
        if regular_graph[n][i] == True:
            row += [ i ]
    print(row)
        
        

    
