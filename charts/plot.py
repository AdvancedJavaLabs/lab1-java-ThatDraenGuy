import os
import matplotlib.pyplot as plt


x = [1, 2, 4, 6, 8, 16, 32]
y = [4853, 2479, 1468, 1147, 967, 990, 987]

plt.xlabel("Число потоков")
plt.ylabel("Время действия алгоритма")
plt.title("Время работы алгоритма в зависимости от выделенных ресурсов")
plt.axis([0, x[len(x) - 1], 0, max(y)])
plt.plot(x, y, **{"color": "lightsteelblue", "marker": "o"})
plt.savefig("threads")



x = [100, 1000, 10000, 50000, 100000, 1000000, 2000000]
ys = [0, 1, 3, 42, 40, 717, 947]
yp = [1, 3, 5, 34, 37, 527, 764]

plt.xlabel("Число вершин графа")
plt.ylabel("Время действия алгоритма")
plt.title("Время работы алгоритма в зависимости от выделенных ресурсов")
plt.axis([0, x[len(x) - 1], 0, max(ys)])
plt.plot(x, ys, **{"color": "lightsteelblue", "marker": "o"})
plt.plot(x, yp, **{"color": "red", "marker": "o"})
plt.savefig("nodes")