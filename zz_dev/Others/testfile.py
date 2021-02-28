import sys
import numpy as np
import tensorflow as tf
from datetime import datetime
device_name = "gpu"  # Choose device - Options: gpu or cpu
shape = (int(100), int(100))
if device_name == "gpu":
    device_name = "/gpu:0"
else:
    device_name = "/cpu:0"
tf.compat.v1.disable_eager_execution()
with tf.device(device_name):
    random_matrix = tf.random.uniform(shape=shape, minval=0, maxval=1)
    dot_operation = tf.matmul(random_matrix, tf.transpose(random_matrix))
    sum_operation = tf.reduce_sum(dot_operation)
startTime = datetime.now()
with tf.compat.v1.Session(config=tf.compat.v1.ConfigProto(log_device_placement=True)) as session:
        result = session.run(sum_operation)
        print(result)
# Print the results
print("Shape:", shape, "Device:", device_name)
print("Time taken:", datetime.now() - startTime)