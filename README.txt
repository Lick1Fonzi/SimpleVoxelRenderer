Struttura del sorgente:

- MainActivity contiene due schermate, la principale e la scelta dell'immagine da disegnare.

Qui è possibile scegliere che renderer utilizzare
- VoxelRenderer: disegna voxel custom a 8 vertici ma senza luce
- InstancingVoxelRenderer: disegna voxel a partire da cube.ply con luce utilizzando instancing

entrambi sono avviati da Voxel_Unimore_Activity

- Vly_Parser e corrispettivo instanced, per parsare i file .vly e creare le strutture dati
- Voxel è l'implementazione di un cubo, utilizzato in VoxelRenderer

Presi da materiale del corso/progetto:
- PlyObject
- ShaderCompiler
- BasicRenderer