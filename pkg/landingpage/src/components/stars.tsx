import { useState, useRef } from 'react'
import { Canvas, useFrame } from '@react-three/fiber'
import { Points, PointMaterial } from '@react-three/drei'
import * as random from 'maath/random'

export default function StarRenderer() {
  return (
    <Canvas
		style={{ position: 'absolute', top: 0, left: 0, width: '100vw', height: '100vh' }}
		camera={{ position: [0, 0, 1] }}>
      <Stars />
    </Canvas>
  )
}

function Stars(props) {

  const ref = useRef<THREE.Points>()
  const [sphere] = useState(() => random.inSphere(new Float32Array(5000), { radius: 1.5 }))

  useFrame((state, delta) => {
	if(ref.current) {
		ref.current.rotation.x -= delta / 10
		ref.current.rotation.y -= delta / 15
	}
  })

  return (
    <group rotation={[0, 0, Math.PI / 4]}>
      <Points ref={ref} positions={sphere} stride={3} frustumCulled={false} {...props}>
        <PointMaterial transparent color="#ffa0e0" size={0.005} sizeAttenuation={true} depthWrite={false} />
      </Points>
    </group>
  )
}
